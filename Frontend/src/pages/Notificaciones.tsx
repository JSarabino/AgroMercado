import React, { useState } from 'react';
import { mockNotificaciones } from '../data/mockData';
import { Bell, Package, Truck, AlertCircle, CheckCircle } from 'lucide-react';

const Notificaciones: React.FC = () => {
  const [notificaciones, setNotificaciones] = useState(mockNotificaciones);

  const marcarComoLeida = (id: string) => {
    setNotificaciones(prev =>
      prev.map(n => n.id === id ? { ...n, leida: true } : n)
    );
  };

  const marcarTodasLeidas = () => {
    setNotificaciones(prev =>
      prev.map(n => ({ ...n, leida: true }))
    );
  };

  const getIcon = (tipo: string) => {
    switch (tipo) {
      case 'pedido':
        return <Package size={24} />;
      case 'entrega':
        return <Truck size={24} />;
      case 'sistema':
        return <AlertCircle size={24} />;
      case 'recordatorio':
        return <Bell size={24} />;
      default:
        return <Bell size={24} />;
    }
  };

  const formatearFecha = (fecha: string) => {
    const date = new Date(fecha);
    const ahora = new Date();
    const diff = ahora.getTime() - date.getTime();
    const minutos = Math.floor(diff / 60000);
    const horas = Math.floor(diff / 3600000);
    const dias = Math.floor(diff / 86400000);

    if (minutos < 60) return `Hace ${minutos} minutos`;
    if (horas < 24) return `Hace ${horas} horas`;
    return `Hace ${dias} días`;
  };

  const noLeidas = notificaciones.filter(n => !n.leida).length;

  return (
    <div className="notificaciones-page">
      <div className="notificaciones-header">
        <h1>🔔 Notificaciones</h1>
        {noLeidas > 0 && (
          <button className="btn-secondary" onClick={marcarTodasLeidas}>
            Marcar todas como leídas ({noLeidas})
          </button>
        )}
      </div>

      <div className="notificaciones-list">
        {notificaciones.length > 0 ? (
          notificaciones.map(notif => (
            <div
              key={notif.id}
              className={`notificacion-card ${!notif.leida ? 'no-leida' : ''}`}
              onClick={() => marcarComoLeida(notif.id)}
            >
              <div className={`notif-icon tipo-${notif.tipo}`}>
                {getIcon(notif.tipo)}
              </div>

              <div className="notif-content">
                <div className="notif-header">
                  <h3>{notif.titulo}</h3>
                  <span className="notif-tiempo">{formatearFecha(notif.fecha)}</span>
                </div>
                <p className="notif-mensaje">{notif.mensaje}</p>
                {notif.accion && (
                  <button className="notif-accion">{notif.accion}</button>
                )}
              </div>

              {!notif.leida && (
                <div className="notif-badge">
                  <CheckCircle size={20} />
                </div>
              )}
            </div>
          ))
        ) : (
          <div className="no-notificaciones">
            <Bell size={64} />
            <p>No tienes notificaciones</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Notificaciones;
