import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-icon',
  standalone: false,
  templateUrl: './icon.html',
  styleUrl: './icon.scss',
})
export class IconComponent {
  @Input() icon: string = ''; // Ejemplo: 'fa-solid fa-eye'
}
