import React from 'react';
import { MapPin, Star, Leaf, Award } from 'lucide-react';

const Productores: React.FC = () => {
  const productores = [
    {
      id: 'p1',
      nombre: 'Finca El Paraíso',
      ubicacion: 'Vereda El Tablón, Popayán',
      calificacion: 4.8,
      productos: 12,
      practicas: ['Orgánico', 'Sin pesticidas', 'Comercio justo'],
      imagen: 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400&h=300&fit=crop',
      verificado: true,
      descripcion: 'Productores de aguacates y frijol orgánico desde hace 15 años.',
    },
    {
      id: 'p2',
      nombre: 'Huerta Don Pedro',
      ubicacion: 'Vereda Las Guacas, Popayán',
      calificacion: 4.9,
      productos: 18,
      practicas: ['Hidropónico', 'Orgánico', 'Agricultura familiar'],
      imagen: 'https://images.unsplash.com/photo-1464226184884-fa280b87c399?w=400&h=300&fit=crop',
      verificado: true,
      descripcion: 'Especialistas en hortalizas frescas y vegetales hidropónicos.',
    },
    {
      id: 'p3',
      nombre: 'Vereda Las Palmas',
      ubicacion: 'Las Palmas, Popayán',
      calificacion: 4.6,
      productos: 8,
      practicas: ['Tradicional', 'Sin químicos', 'Local'],
      imagen: 'https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400&h=300&fit=crop',
      verificado: true,
      descripcion: 'Productores de yuca y papas criollas de excelente calidad.',
    },
    {
      id: 'p4',
      nombre: 'Finca La Esperanza',
      ubicacion: 'Vereda Julumito, Popayán',
      calificacion: 4.7,
      productos: 6,
      practicas: ['Lácteos artesanales', 'Ganadería sostenible', 'Pastos naturales'],
      imagen: 'https://images.unsplash.com/photo-1500595046743-cd271d694d30?w=400&h=300&fit=crop',
      verificado: true,
      descripcion: 'Lácteos frescos y quesos artesanales de producción diaria.',
    },
  ];

  return (
    <div className="productores-page">
      <div className="productores-header">
        <h1>🌾 Nuestros Productores</h1>
        <p>Conoce a los agricultores que hacen posible tu comida</p>
      </div>

      <div className="productores-grid">
        {productores.map(productor => (
          <div key={productor.id} className="productor-card">
            <div className="productor-image">
              <img src={productor.imagen} alt={productor.nombre} />
              {productor.verificado && (
                <div className="verificado-badge">
                  <Award size={16} />
                  Verificado
                </div>
              )}
            </div>

            <div className="productor-content">
              <h2>{productor.nombre}</h2>
              <p className="productor-ubicacion">
                <MapPin size={16} />
                {productor.ubicacion}
              </p>

              <div className="productor-rating">
                <Star size={16} fill="#ffc107" color="#ffc107" />
                <span>{productor.calificacion}</span>
                <span className="productos-count">• {productor.productos} productos</span>
              </div>

              <p className="productor-descripcion">{productor.descripcion}</p>

              <div className="productor-practicas">
                {productor.practicas.map((practica, index) => (
                  <span key={index} className="practica-tag">
                    <Leaf size={14} />
                    {practica}
                  </span>
                ))}
              </div>

              <button className="btn-primary btn-block">Ver Productos</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Productores;
