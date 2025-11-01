import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import afiliacionesService from '../services/afiliaciones.service';
import type { AfiliacionResumen } from '../services/afiliaciones.service';
import { FileText, Clock, CheckCircle, XCircle, Calendar, MapPin, ArrowLeft } from 'lucide-react';

const MisSolicitudesAfiliacion: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [solicitudes, setSolicitudes] = useState<AfiliacionResumen[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarSolicitudes();
  }, [user]);

  const cargarSolicitudes = async () => {
    if (!user?.id) return;

    try {
      setLoading(true);
      const data = await afiliacionesService.listarMisSolicitudes(user.id);
      setSolicitudes(data);
    } catch (err) {
      console.error('Error al cargar solicitudes:', err);
      setError('No se pudieron cargar las solicitudes');
    } finally {
      setLoading(false);
    }
  };

  const getEstadoClase = (estado: string) => {
    switch (estado) {
      case 'PENDIENTE':
        return 'estado-pendiente';
      case 'APROBADA':
        return 'estado-aprobada';
      case 'RECHAZADA':
        return 'estado-rechazada';
      default:
        return '';
    }
  };

  const getEstadoIcono = (estado: string) => {
    switch (estado) {
      case 'PENDIENTE':
        return <Clock size={20} />;
      case 'APROBADA':
        return <CheckCircle size={20} />;
      case 'RECHAZADA':
        return <XCircle size={20} />;
      default:
        return null;
    }
  };

  const formatFecha = (fecha: string) => {
    return new Date(fecha).toLocaleDateString('es-CO', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="mis-solicitudes-page">
      <div className="page-header">
        <button
          className="btn-back"
          onClick={() => navigate('/admin')}
        >
          <ArrowLeft size={20} />
          Volver al Dashboard
        </button>
        <h1>
          <FileText size={32} />
          Mis Solicitudes de Afiliación
        </h1>
        <p className="page-subtitle">
          Revisa el estado de tus solicitudes de zonas
        </p>
      </div>

      {error && (
        <div className="alert alert-error">
          {error}
        </div>
      )}

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Cargando solicitudes...</p>
        </div>
      ) : solicitudes.length === 0 ? (
        <div className="empty-state">
          <FileText size={64} />
          <h3>No tienes solicitudes</h3>
          <p>Aún no has enviado ninguna solicitud de afiliación de zona</p>
          <button
            className="btn-primary"
            onClick={() => navigate('/admin/solicitar-afiliacion')}
          >
            Solicitar Afiliación
          </button>
        </div>
      ) : (
        <div className="table-container">
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
                <tr key={solicitud.afiliacionId} className={getEstadoClase(solicitud.estado)}>
                  <td className="solicitud-id-cell">
                    <code>{solicitud.afiliacionId}</code>
                  </td>
                  <td className="vereda-cell">
                    <div className="cell-with-icon">
                      <MapPin size={16} />
                      <strong>{solicitud.nombreVereda}</strong>
                    </div>
                  </td>
                  <td>{solicitud.municipio}</td>
                  <td className="zona-cell">
                    {solicitud.zonaId ? (
                      <span className="zona-badge">{solicitud.zonaId}</span>
                    ) : (
                      <span className="zona-pendiente">Sin asignar</span>
                    )}
                  </td>
                  <td>
                    <span className={`badge-estado ${getEstadoClase(solicitud.estado)}`}>
                      {getEstadoIcono(solicitud.estado)}
                      {solicitud.estado}
                    </span>
                  </td>
                  <td className="fecha-cell">
                    <div className="cell-with-icon">
                      <Calendar size={16} />
                      {formatFecha(solicitud.updatedAt)}
                    </div>
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

        .page-header h1 {
          display: flex;
          align-items: center;
          gap: 1rem;
          font-size: 2rem;
          color: #333;
          margin-bottom: 0.5rem;
        }

        .page-subtitle {
          color: #666;
          font-size: 1.1rem;
        }

        .btn-back {
          display: inline-flex;
          align-items: center;
          gap: 0.5rem;
          padding: 0.5rem 1rem;
          background: transparent;
          border: 1px solid #ddd;
          border-radius: 5px;
          color: #666;
          cursor: pointer;
          margin-bottom: 1rem;
          transition: all 0.3s;
        }

        .btn-back:hover {
          background: #f5f5f5;
          border-color: #4CAF50;
          color: #4CAF50;
        }

        .table-container {
          background: white;
          border-radius: 10px;
          box-shadow: 0 2px 8px rgba(0,0,0,0.1);
          overflow: hidden;
        }

        .solicitudes-table {
          width: 100%;
          border-collapse: collapse;
        }

        .solicitudes-table thead {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }

        .solicitudes-table th {
          padding: 1rem;
          text-align: left;
          font-weight: 600;
          font-size: 0.9rem;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }

        .solicitudes-table tbody tr {
          border-bottom: 1px solid #f0f0f0;
          transition: background-color 0.3s;
        }

        .solicitudes-table tbody tr:hover {
          background-color: #f8f9fa;
        }

        .solicitudes-table tbody tr.estado-pendiente {
          border-left: 4px solid #FFA726;
        }

        .solicitudes-table tbody tr.estado-aprobada {
          border-left: 4px solid #4CAF50;
        }

        .solicitudes-table tbody tr.estado-rechazada {
          border-left: 4px solid #EF5350;
        }

        .solicitudes-table td {
          padding: 1rem;
          vertical-align: middle;
        }

        .solicitud-id-cell code {
          background: #f5f5f5;
          padding: 0.3rem 0.6rem;
          border-radius: 4px;
          font-size: 0.85rem;
          color: #666;
          font-family: 'Courier New', monospace;
        }

        .cell-with-icon {
          display: flex;
          align-items: center;
          gap: 0.5rem;
        }

        .cell-with-icon svg {
          color: #4CAF50;
          flex-shrink: 0;
        }

        .vereda-cell strong {
          color: #333;
        }

        .zona-badge {
          background: #E8F5E9;
          color: #2E7D32;
          padding: 0.3rem 0.8rem;
          border-radius: 15px;
          font-size: 0.85rem;
          font-weight: 600;
          display: inline-block;
        }

        .zona-pendiente {
          color: #999;
          font-style: italic;
          font-size: 0.9rem;
        }

        .badge-estado {
          display: inline-flex;
          align-items: center;
          gap: 0.5rem;
          padding: 0.4rem 0.8rem;
          border-radius: 20px;
          font-size: 0.85rem;
          font-weight: 600;
        }

        .badge-estado.estado-pendiente {
          background: #FFF3E0;
          color: #F57C00;
        }

        .badge-estado.estado-aprobada {
          background: #E8F5E9;
          color: #2E7D32;
        }

        .badge-estado.estado-rechazada {
          background: #FFEBEE;
          color: #C62828;
        }

        .fecha-cell {
          color: #666;
          font-size: 0.9rem;
        }

        .empty-state {
          text-align: center;
          padding: 4rem 2rem;
          background: white;
          border-radius: 10px;
          box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .empty-state svg {
          color: #ddd;
          margin-bottom: 1rem;
        }

        .empty-state h3 {
          font-size: 1.5rem;
          color: #333;
          margin-bottom: 0.5rem;
        }

        .empty-state p {
          color: #666;
          margin-bottom: 2rem;
        }

        .loading-container {
          text-align: center;
          padding: 4rem 2rem;
        }

        .spinner {
          border: 4px solid #f3f3f3;
          border-top: 4px solid #4CAF50;
          border-radius: 50%;
          width: 50px;
          height: 50px;
          animation: spin 1s linear infinite;
          margin: 0 auto 1rem;
        }

        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }

        .alert {
          padding: 1rem;
          border-radius: 8px;
          margin-bottom: 1.5rem;
        }

        .alert-error {
          background: #FFEBEE;
          color: #C62828;
          border: 1px solid #FFCDD2;
        }

        @media (max-width: 1024px) {
          .table-container {
            overflow-x: auto;
          }

          .solicitudes-table {
            min-width: 800px;
          }
        }

        @media (max-width: 768px) {
          .mis-solicitudes-page {
            padding: 1rem;
          }

          .page-header h1 {
            font-size: 1.5rem;
          }

          .solicitudes-table th,
          .solicitudes-table td {
            padding: 0.75rem 0.5rem;
            font-size: 0.85rem;
          }

          .cell-with-icon {
            flex-direction: column;
            align-items: flex-start;
            gap: 0.25rem;
          }
        }
      `}</style>
    </div>
  );
};

export default MisSolicitudesAfiliacion;
