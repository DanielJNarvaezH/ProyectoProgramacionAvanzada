import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-label',
  standalone: false,
  templateUrl: './label.html',
  styleUrl: './label.scss',
})
export class LabelComponent {
  @Input() text: string = '';
  @Input() for: string = '';
}

