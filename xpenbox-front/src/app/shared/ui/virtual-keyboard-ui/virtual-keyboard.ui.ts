import { Component, input, OnInit, output, signal } from '@angular/core';

@Component({
  selector: 'app-virtual-keyboard-ui',
  imports: [],
  templateUrl: './virtual-keyboard.ui.html',
  styleUrl: './virtual-keyboard.ui.css',
})
export class VirtualKeyboardUi implements OnInit {

  // Input/Ouput properties
  currency = input<string>('PEN');
  defaultAmount = input<number>(0);
  amountOutput = output<number>();

  // Internal signal to hold the current amount as a string
  amount = signal('');

  // Numeric keyboard keys (1-9 and decimal point)
  keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '.'];

  ngOnInit(): void {
    if (this.defaultAmount() > 0) {
      this.amount.set(this.defaultAmount().toFixed(2));
      this.emitAmount();
    }
  }

  /**
   * Handle key press from numeric keyboard
   * @param key The key that was pressed
   * @returns void
   */
  onKeyPress(key: string): void {
    const value = this.amount();
    
    // Validate decimal point
    if (key === '.' && value.includes('.')) return;

    // Avoid multiple leading zeros
    if (value === '0' && key === '0') return;
    
    // Limit length
    if (value.length >= 9) return;

    // If decimal point is pressed without a value, add 0.
    if (key === '.' && !value) {
      this.amount.set('0.');
      return;
    }

    // Avoid leading zeros
    if (key === '0' && !value) {
      this.amount.set(key);
      return;
    }
    this.amount.set(value + key);

    this.emitAmount();
  }

  /**
   * Handle backspace key press
   * @returns void
   */
  onBackspace(): void {
    const value = this.amount();
    this.amount.set(value.slice(0, -1));
    this.emitAmount();
  }

  /**
   * Handle clear key press
   * @returns void
   */
  onClear(): void {
    this.amount.set('');
    this.emitAmount();
  }

  /**
   * Emit the current amount as a number
   * @returns void
   */
  private emitAmount(): void {
    const value = this.amount();
    const numericValue = parseFloat(value);
    this.amountOutput.emit(isNaN(numericValue) ? 0 : numericValue);
  }
}
