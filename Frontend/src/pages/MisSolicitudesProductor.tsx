import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import afiliacionesService from '../services/afiliaciones.service';
import type { AfiliacionResumen } from '../services/afiliaciones.service';
import { CheckCircle, XCircle, Clock, MapPin, ArrowLeft } from 'lucide-react';

const MisSolicitudesProductor: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [solicitudes, setSolicitudes] = useState<AfiliacionResumen[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const cargarSolicitudes = async () => {
      if (!user?.id) {
        console.error('No hay usuario autenticado');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const data = await afiliacionesService.listarMisSolicitudes(user.id);
        setSolicitudes(data);
      } catch (error) {
        console.error('Error cargando solicitudes:', error);
        alert('Error al cargar las solicitudes. Por favor intenta nuevamente.');
      } finally {
        setLoading(false);
      }
    };

    cargarSolicitudes();
  }, [user]);

  const getEstadoColor = (estado: string) => {
    switch (estado) {
      case 'PENDIENTE':
        return '#fbbf24'; // Amarillo
      case 'APROBADA':
        return '#10b981'; // Verde
      case 'RECHAZADA':
        return '#ef4444'; // Rojo
      default:
        return '#9ca3af'; // Gris
    }
  };

  const getEstadoIcon = (estado: string) => {
    switch (estado) {
      case 'PENDIENTE':
        return <Clock size={20} />;
      case 'APROBADA':
        return <CheckCircle size={20} />;
      case 'RECHAZADA':
        return <XCircle size={20} />;
      default:
        return <Clock size={20} />;
    }
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner"></div>
        <p>Cargando tus solicitudes...</p>
      </div>
    );
  }

  return (
    <div className="mis-solicitudes-page">
      <div className="page-header">
        <button className="btn-back" onClick={() => navigate('/productor')}>
          <ArrowLeft size={20} />
          Volver al Dashboard
        </button>
        <h1>📋 Mis Solicitudes de Afiliación</h1>
        <p>Revisa el estado de tus solicitudes de afiliación a zonas productivas</p>
      </div>

      {solicitudes.length === 0 ? (
        <div className="empty-state">
          <MapPin size={64} color="#cbd5e0" />
          <h2>No tienes solicitudes de afiliación</h2>
          <p>Aún no has enviado ninguna solicitud de afiliación a una zona productiva</p>
          <button
            className="btn-primary"
            onClick={() => navigate('/productor/solicitar-afiliacion')}
          >
            <MapPin size={20} />
            Solicitar Afiliación Ahora
          </button>
        </div>
      ) : (
        <div className="table-wrapper">
          <table className="solicitudes-table">
            <thead>
              <tr>
                <th>ID Solicitud</th>
                <th>Vereda</th>
                <th>Municipio</th>
                <th>Zona Asignada</th>
                <th>Estado</th>
                <th>Última Actualización</th>
              </tr>
            </thead>
            <tbody>
              {solicitudes.map((solicitud) => (
                <tr
                  key={solicitud.afiliacionId}
                  style={{
                    borderLeft: `4px solid ${getEstadoColor(solicitud.estado)}`,
                  }}
                >
                  <td>
                    <code className="solicitud-id">{solicitud.afiliacionId}</code>
                  </td>
                  <td>
                    <div className="cell-with-icon">
                      <MapPin size={16} />
                      <strong>{solicitud.nombreVereda}</strong>
                    </div>
                  </td>
                  <td>{solicitud.municipio}</td>
                  <td>
                    {solicitud.zonaId ? (
                      <span className="zona-badge">{solicitud.zonaId}</span>
                    ) : (
                      <span className="zona-pendiente">Pendiente de asignación</span>
                    )}
                  </td>
                  <td>
                    <span
                      className="estado-badge"
                      style={{
                        backgroundColor: `${getEstadoColor(solicitud.estado)}20`,
                        color: getEstadoColor(solicitud.estado),
                        border: `2px solid ${getEstadoColor(solicitud.estado)}`,
                      }}
                    >
                      {getEstadoIcon(solicitud.estado)}
                      {solicitud.estado}
                    </span>
                  </td>
                  <td>
                    {new Date(solicitud.updatedAt).toLocaleString('es-CO', {
                      year: 'numeric',
                      month: 'short',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <style>{`
        .mis-solicitudes-page {
          padding: 2rem;
          max-width: 1400px;
          margin: 0 auto;
        }

        .page-header {
          margin-bottom: 2rem;
        }

        .btn-back {
          display: inline-flex;
          align-items: center;
          gap: 0.5rem;
          padding: 0.625rem 1rem;
          background: white;
          border: 2px solid var(--border);
          border-radius: 0.5rem;
          color: var(--text);
          cursor: pointer;
          font-weight: 500;
          margin-bottom: 1rem;
          transition: all 0.2s;
        }

        .btn-back:hover {
          background: var(--background);
          border-color: var(--primary);
        }

        .page-header h1 {
          margin: 0.5rem 0;
          color: #2d3748;
        }

        .page-header p {
          color: #718096;
          margin: 0;
        }

        .empty-state {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 4rem 2rem;
          text-align: center;
          background: white;
          border-radius: 12px;
          box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .empty-state h2 {
          margin: 1rem 0 0.5rem 0;
          color: #2d3748;
        }

        .empty-state p {
          color: #718096;
          margin-bottom: 2rem;
        }

        .table-wrapper {
          background: white;
          border-radius: 12px;
          box-shadow: 0 2px 8px rgba(0,0,0,0.1);
          overflow: hidden;
        }

        .solicitudes-table {
          width: 100%;
          border-collapse: collapse;
        }

        .solicitudes-table thead {
          background: #f7fafc;
          border-bottom: 2px solid #e2e8f0;
        }

        .solicitudes-table th {
          padding: 1rem;
          text-align: left;
          font-weight: 600;
          color: #4a5568;
          font-size: 0.875rem;
          text-transform: uppercase;
          letter-spacing: 0.05em;
        }

        .solicitudes-table tbody tr {
          border-bottom: 1px solid #e2e8f0;
          transition: background-color 0.2s;
        }

        .solicitudes-table tbody tr:hover {
          background: #f9fafb;
        }

        .solicitudes-table td {
          padding: 1rem;
          color: #2d3748;
        }

        .solicitud-id {
          background: #edf2f7;
          padding: 0.25rem 0.5rem;
          border-radius: 4px;
          font-size: 0.875rem;
          color: #4a5568;
          font-family: 'Courier New', monospace;
        }

        .cell-with-icon {
          display: flex;
          align-items: center;
          gap: 0.5rem;
        }

        .zona-badge {
          background: #dbeafe;
          color: #1e40af;
          padding: 0.375rem 0.75rem;
          border-radius: 6px;
          font-weight: 500;
          font-size: 0.875rem;
        }

        .zona-pendiente {
          color: #9ca3af;
          font-style: italic;
          font-size: 0.875rem;
        }

        .estado-badge {
          display: inline-flex;
          align-items: center;
          gap: 0.5rem;
          padding: 0.5rem 1rem;
          border-radius: 20px;
          font-size: 0.875rem;
          font-weight: 600;
          text-transform: uppercase;
          letter-spacing: 0.05em;
        }

        .loading-screen {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          min-height: 400px;
          gap: 1rem;
        }

        .spinner {
          width: 50px;
          height: 50px;
          border: 4px solid #e2e8f0;
          border-top-color: #4caf50;
          border-radius: 50%;
          animation: spin 1s linear infinite;
        }

        @keyframes spin {
          to { transform: rotate(360deg); }
        }

        @media (max-width: 768px) {
          .mis-solicitudes-page {
            padding: 1rem;
          }

          .table-wrapper {
            overflow-x: auto;
          }

          .solicitudes-table {
            font-size: 0.875rem;
          }

          .solicitudes-table th,
          .solicitudes-table td {
            padding: 0.75rem 0.5rem;
          }
        }
      `}</style>
    </div>
  );
};

export default MisSolicitudesProductor;
