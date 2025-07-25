package com.project.shopapp.components;

import com.project.shopapp.exceptions.InvalidParamException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
    @Value("${jwt.expiration}")
    private int expiration; //lưu vào environment variable

    @Value("${jwt.secretKey}")
    private String secretKey;

    public String generateToken(com.project.shopapp.models.User user) throws Exception{
        //properties =>
        Map<String, Object> claims = new HashMap<>();
        //this.generateSecretKey();
        claims.put("phoneNumber", user.getPhoneNumber());
        try{
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getPhoneNumber())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact();
            return token;
        } catch (Exception e) {
            //có thể dùng Logger thay vì System.out.println
            throw new InvalidParamException("Cannot create jwt token, error" + e.getMessage());
            //return null;
        }
    }
    private Key getSignKey(){
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        //Keys.hmacShaKeyFor(Decoders.BASE64.decode("1FQbKe8PtU6rh2XTHS7l5qeUMFVElyOhuhYW5J+ZWig="))
        return Keys.hmacShaKeyFor(bytes);
    }

    private String generateSecretKey(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] ketBytes = new byte[32]; //256-bit key
        secureRandom.nextBytes(ketBytes);
        String secretKey = Encoders.BASE64.encode(ketBytes);
        return secretKey;
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey()) //truyền key để sinh ra claims
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public  <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    //check expiration
    public boolean isTokenExpired(String token){
        Date expirationDate = this.extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }
    public String extractPhoneNumber(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails){     //ktra username và token còn hạn ko
        String phoneNumber = extractPhoneNumber(token);
        return (phoneNumber.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
}
