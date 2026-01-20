import { Component } from '@angular/core';
import { AccountCard } from '../../shared/cards/account-card/account.card';

@Component({
  selector: 'app-account-page',
  imports: [AccountCard],
  templateUrl: './account.page.html',
  styleUrl: './account.page.css',
})
export class AccountPage {

}
