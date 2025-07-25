import { Component } from '@angular/core';
import { environment } from 'src/app/environments/environment';
import { Product } from 'src/app/models/product';
import { CartService } from 'src/app/services/cart.service';
import { ProductService } from 'src/app/services/product.service';

@Component({
  selector: 'app-order-confirm',
  templateUrl: './order-confirm.component.html',
  styleUrls: ['./order-confirm.component.scss']
})
export class OrderConfirmComponent {
  ngOnInit(): void {

  }
}
