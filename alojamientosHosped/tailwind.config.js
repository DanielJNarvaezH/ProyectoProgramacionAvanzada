/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class',
  content: [
    "./src/**/*.{html,ts,scss}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50:  '#FCEBFF',
          100: '#F7CCFF',
          200: '#EFA8FF',
          300: '#E784FF',
          400: '#DE4BFF',
          500: '#CE0DEB', // color principal
          600: '#B10CCF',
          700: '#910BAF',
          800: '#6E0988',
          900: '#4D0762',
        },
        secondary: {
          50:  '#F2EDF3',
          100: '#E0D6E2',
          200: '#C3B5C7',
          300: '#A495A9',
          400: '#918396', // color base secundario
          500: '#7E7284',
          600: '#6B6272',
          700: '#554C5B',
          800: '#3D3742',
          900: '#27242B',
        },
        tertiary: {
          50:  '#F6EFF9',
          100: '#E9D9EF',
          200: '#D1B2DF',
          300: '#B98BCF',
          400: '#A369C0',
          500: '#B188C0', // color base terciario
          600: '#8E6D9C',
          700: '#6E5379',
          800: '#4D3956',
          900: '#2D2033',
        },
        neutral: {
          50:  '#F5F2F6',
          100: '#E7E1E9',
          200: '#CFC2D3',
          300: '#B7A3BC',
          400: '#9E85A6',
          500: '#645168', // color base neutral
          600: '#513F53',
          700: '#3E2E3E',
          800: '#2A1D29',
          900: '#160D15',
        },
        'neutral-dark': '#381335',
        success: '#28A745',
        error: '#DC3545',
        warning: '#FFC107',
        info: '#17A2B8',
      },
      fontFamily: {
        heading: ['Montserrat', 'sans-serif'],
        body: ['Roboto', 'sans-serif'],
        cta: ['Poppins', 'sans-serif'],
        label: ['Lato', 'sans-serif'],
        caption: ['Open Sans', 'sans-serif'],
      },
      spacing: {
        'xs': '4px',
        'sm': '8px',
        'md': '16px',
        'lg': '24px',
        'xl': '32px',
        '2xl': '48px',
      },
    },
  },
  plugins: [],
}
