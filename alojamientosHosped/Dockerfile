# Etapa 1: Construcción de Angular
FROM node:22 AS build
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

# Angular 17/18 ya no usa --prod
RUN npm run build --configuration production

# Etapa 2: Servidor Nginx
FROM nginx:alpine
COPY --from=build /app/dist/alojamientosHosped/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
