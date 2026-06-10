# Frontend - AgroMercado

Aplicación web de AgroMercado desarrollada con React, TypeScript y Vite.

Este frontend permite la interacción de los usuarios con la plataforma, incluyendo navegación, autenticación, consumo de APIs REST y visualización de funcionalidades relacionadas con productores, compradores, productos y pedidos.

---

## Tecnologías

* React
* TypeScript
* Vite
* HTML
* CSS
* APIs REST
* JWT

---

## Estructura general

```txt
Frontend/
│
├── public/
├── src/
│   ├── assets/
│   ├── components/
│   ├── config/
│   ├── context/
│   ├── data/
│   ├── pages/
│   ├── services/
│   ├── styles/
│   └── types/
│
├── Dockerfile
├── nginx.conf
├── package.json
└── vite.config.ts
```

---

## Instalación

```bash
npm install
```

---

## Ejecución en desarrollo

```bash
npm run dev
```

---

## Compilación para producción

```bash
npm run build
```

---

## Variables de entorno

El proyecto debe usar un archivo `.env` local basado en un archivo de ejemplo.

Ejemplo:

```bash
cp .env.example .env
```

No se recomienda subir archivos `.env` reales al repositorio.
