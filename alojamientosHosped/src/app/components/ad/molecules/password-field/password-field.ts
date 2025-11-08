import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-password-field',
  standalone: false,
  templateUrl: './password-field.html',
  styleUrl: './password-field.scss',
})
export class PasswordFieldComponent {
  @Input() id: string = '';
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() value: string = '';
  showPassword: boolean = false;

  togglePassword() {
    this.showPassword = !this.showPassword;
  }
}
