import { Component } from '@angular/core';
import { MenuComponent } from '../../shared/components/menu-component/menu.component';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-landing-page',
  imports: [MenuComponent, RouterOutlet],
  templateUrl: './landing.page.html',
  styleUrl: './landing.page.css',
})
export class LandingPage {

}
