import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-input-field',
  standalone: false,
  templateUrl: './input-field.html',
  styleUrl: './input-field.scss',
})
export class InputFieldComponent {
  @Input() id: string = '';
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() type: string = 'text';
  @Input() value: string = '';
}
