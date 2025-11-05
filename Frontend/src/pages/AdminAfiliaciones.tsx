import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import afiliacionesService from '../services/afiliaciones.service';
import type { AfiliacionResumen } from '../services/afiliaciones.service';
import { CheckCircle, XCircle, Clock, MapPin, User } from 'lucide-react';

const AdminAfiliaciones: React.FC = () => {
  const [afiliaciones, setAfiliaciones] = useState<AfiliacionResumen[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'TODAS'>('PENDIENTE');
  const [selectedAfiliacion, setSelectedAfiliacion] = useState<AfiliacionResumen | null>(null);
  const [observaciones, setObservaciones] = useState('');
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    cargarAfiliaciones();
  }, []);

  const cargarAfiliaciones = async () => {
    try {
      setLoading(true);
      // Obtener todas las solicitudes desde MongoDB (solo Admin Global puede ver todas)
      const data = await afiliacionesService.listarTodasLasSolicitudes();
      setAfiliaciones(data);
    } catch (error) {
      console.error('Error cargando afiliaciones:', error);
      alert('Error al cargar las solicitudes. Por favor intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  const handleAprobar = async (afiliacionId: string) => {
    if (!confirm('¿Está seguro de aprobar esta solicitud de zona?')) return;

    try {
      setProcessing(true);
      await afiliacionesService.aprobarAfiliacion(afiliacionId, observaciones);
      alert('✅ Solicitud aprobada exitosamente.');
      setSelectedAfiliacion(null);
      setObservaciones('');
      cargarAfiliaciones();
    } catch (error) {
      console.error('Error aprobando:', error);
      alert('❌ Error al aprobar la solicitud');
    } finally {
      setProcessing(false);
    }
  };

  const handleRechazar = async (afiliacionId: string) => {
    if (!observaciones.trim()) {
      alert('Debe ingresar observaciones para rechazar');
      return;
    }

    if (!confirm('¿Está seguro de rechazar esta solicitud?')) return;

    try {
      setProcessing(true);
      await afiliacionesService.rechazarAfiliacion(afiliacionId, observaciones);
      alert('❌ Solicitud rechazada');
      setSelectedAfiliacion(null);
      setObservaciones('');
      cargarAfiliaciones();
    } catch (error) {
      console.error('Error rechazando:', error);
      alert('❌ Error al rechazar la solicitud');
    } finally {
      setProcessing(false);
    }
  };

  const afiliacionesFiltradas = filter === 'TODAS'
    ? afiliaciones
    : afiliaciones.filter(a => a.estado === filter);

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner"></div>
        <p>Cargando solicitudes...</p>
      </div>
    );
  }

  return (
    <div className="admin-afiliaciones-page">
      <div className="page-header">
        <div>
          <h1>🌾 Gestión de Afiliaciones de Zonas</h1>
          <p>Administre las solicitudes para crear nuevas zonas productivas</p>
        </div>
      </div>

      <div className="filters-bar">
        <button className={filter === 'TODAS' ? 'active' : ''} onClick={() => setFilter('TODAS')}>
          Todas
        </button>
        <button className={filter === 'PENDIENTE' ? 'active' : ''} onClick={() => setFilter('PENDIENTE')}>
          <Clock size={16} />
          Pendientes
        </button>
        <button className={filter === 'APROBADA' ? 'active' : ''} onClick={() => setFilter('APROBADA')}>
          <CheckCircle size={16} />
          Aprobadas
        </button>
        <button className={filter === 'RECHAZADA' ? 'active' : ''} onClick={() => setFilter('RECHAZADA')}>
          <XCircle size={16} />
          Rechazadas
        </button>
      </div>

      {afiliacionesFiltradas.length > 0 ? (
        <div className="table-container">
          <table className="solicitudes-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Vereda</th>
                <th>Municipio</th>
                <th>Solicitante</th>
                <th>Estado</th>
                <th>Zona Asignada</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {afiliacionesFiltradas.map(afiliacion => (
                <tr key={afiliacion.afiliacionId}>
                  <td>
                    <code>{afiliacion.afiliacionId}</code>
                  </td>
                  <td>
                    <div className="cell-with-icon">
                      <MapPin size={16} />
                      <strong>{afiliacion.nombreVereda}</strong>
                    </div>
                  </td>
                  <td>{afiliacion.municipio}</td>
                  <td>
                    <div className="cell-with-icon">
                      <User size={16} />
                      <span title={afiliacion.solicitanteUsuarioId}>
                        {afiliacion.representanteNombre || afiliacion.solicitanteUsuarioId}
                      </span>
                    </div>
                  </td>
                  <td>
                    <span className={`badge-estado estado-${afiliacion.estado.toLowerCase()}`}>
                      {afiliacion.estado === 'PENDIENTE' && <Clock size={16} />}
                      {afiliacion.estado === 'APROBADA' && <CheckCircle size={16} />}
                      {afiliacion.estado === 'RECHAZADA' && <XCircle size={16} />}
                      {afiliacion.estado}
                    </span>
                  </td>
                  <td>
                    {afiliacion.zonaId ? (
                      <span className="zona-badge" title={afiliacion.zonaId}>
                        {afiliacion.nombreVereda || afiliacion.zonaId}
                      </span>
                    ) : (
                      <span className="zona-pendiente">Sin asignar</span>
                    )}
                  </td>
                  <td>
                    {afiliacion.estado === 'PENDIENTE' && (
                      <button
                        className="btn-action"
                        onClick={() => setSelectedAfiliacion(afiliacion)}
                        disabled={processing}
                      >
                        Revisar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="no-results">
          <p>No hay solicitudes {filter !== 'TODAS' && `en estado ${filter}`}</p>
        </div>
      )}

      {selectedAfiliacion && createPortal(
        <div className="modal-overlay" onClick={() => setSelectedAfiliacion(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Revisar Solicitud</h2>
              <button className="btn-close" onClick={() => setSelectedAfiliacion(null)}>
                ×
              </button>
            </div>

            <div className="modal-body">
              <div className="form-group">
                <label>Observaciones</label>
                <textarea
                  value={observaciones}
                  onChange={(e) => setObservaciones(e.target.value)}
                  placeholder="Ingrese observaciones..."
                  rows={4}
                />
              </div>
            </div>

            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setSelectedAfiliacion(null)}>
                Cancelar
              </button>
              <button className="btn-danger" onClick={() => handleRechazar(selectedAfiliacion.afiliacionId)}>
                <XCircle size={18} />
                Rechazar
              </button>
              <button className="btn-success" onClick={() => handleAprobar(selectedAfiliacion.afiliacionId)}>
                <CheckCircle size={18} />
                Aprobar
              </button>
            </div>
          </div>
        </div>,
        document.body
      )}

      <style>{`
        .admin-afiliaciones-page {
          padding: 2rem;
          max-width: 1400px;
          margin: 0 auto;
        }

        .page-header {
          margin-bottom: 2rem;
        }

        .page-header h1 {
          margin: 0 0 0.5rem 0;
          color: #2d3748;
        }

        .page-header p {
          color: #718096;
          margin: 0;
        }

        .filters-bar {
          display: flex;
          gap: 1rem;
          margin-bottom: 2rem;
          flex-wrap: wrap;
        }

        .filters-bar button {
          padding: 0.75rem 1.5rem;
          border: 2px solid #e2e8f0;
          background: white;
          border-radius: 8px;
          cursor: pointer;
          display: flex;
          align-items: center;
          gap: 0.5rem;
          transition: all 0.2s;
          font-weight: 500;
        }

        .filters-bar button:hover {
          border-color: #4caf50;
          background: #f0fdf4;
        }

        .filters-bar button.active {
          border-color: #4caf50;
          background: #4caf50;
          color: white;
        }

        .table-container {
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

        .solicitudes-table td code {
          background: #edf2f7;
          padding: 0.25rem 0.5rem;
          border-radius: 4px;
          font-size: 0.875rem;
          color: #4a5568;
        }

        .cell-with-icon {
          display: flex;
          align-items: center;
          gap: 0.5rem;
        }

        .badge-estado {
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

        .estado-pendiente {
          background: #fef3c7;
          color: #92400e;
          border: 2px solid #fbbf24;
        }

        .estado-aprobada {
          background: #d1fae5;
          color: #065f46;
          border: 2px solid #10b981;
        }

        .estado-rechazada {
          background: #fee2e2;
          color: #991b1b;
          border: 2px solid #ef4444;
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
        }

        .btn-action {
          padding: 0.5rem 1rem;
          background: #4caf50;
          color: white;
          border: none;
          border-radius: 6px;
          font-weight: 500;
          cursor: pointer;
          transition: all 0.2s;
        }

        .btn-action:hover:not(:disabled) {
          background: #45a049;
          transform: translateY(-1px);
          box-shadow: 0 4px 8px rgba(76, 175, 80, 0.3);
        }

        .btn-action:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .no-results {
          text-align: center;
          padding: 4rem 2rem;
          color: #718096;
          font-size: 1.125rem;
        }

        .admin-afiliaciones-page .modal-overlay {
          position: fixed;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 2000;
          padding: 1rem;
          box-sizing: border-box;
        }

        .admin-afiliaciones-page .modal {
          background: white;
          border-radius: 12px;
          width: 100%;
          max-width: 600px;
          max-height: 90vh;
          overflow-y: auto;
          box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
          margin: auto;
          position: relative;
        }

        .modal-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 1.5rem;
          border-bottom: 1px solid #e2e8f0;
        }

        .modal-header h2 {
          margin: 0;
          color: #2d3748;
        }

        .btn-close {
          background: none;
          border: none;
          font-size: 2rem;
          cursor: pointer;
          color: #718096;
          padding: 0;
          width: 2rem;
          height: 2rem;
          line-height: 1;
        }

        .btn-close:hover {
          color: #2d3748;
        }

        .modal-body {
          padding: 1.5rem;
        }

        .form-group {
          margin-bottom: 1.5rem;
        }

        .form-group label {
          display: block;
          margin-bottom: 0.5rem;
          font-weight: 600;
          color: #2d3748;
        }

        .form-group textarea {
          width: 100%;
          padding: 0.75rem;
          border: 2px solid #e2e8f0;
          border-radius: 8px;
          font-family: inherit;
          font-size: 1rem;
          resize: vertical;
          transition: border-color 0.2s;
        }

        .form-group textarea:focus {
          outline: none;
          border-color: #4caf50;
        }

        .modal-footer {
          padding: 1.5rem;
          border-top: 1px solid #e2e8f0;
          display: flex;
          justify-content: flex-end;
          gap: 1rem;
        }

        .modal-footer button {
          padding: 0.75rem 1.5rem;
          border: none;
          border-radius: 8px;
          font-weight: 500;
          cursor: pointer;
          display: flex;
          align-items: center;
          gap: 0.5rem;
          transition: all 0.2s;
        }

        .btn-secondary {
          background: #e2e8f0;
          color: #2d3748;
        }

        .btn-secondary:hover {
          background: #cbd5e0;
        }

        .btn-danger {
          background: #ef4444;
          color: white;
        }

        .btn-danger:hover {
          background: #dc2626;
        }

        .btn-success {
          background: #4caf50;
          color: white;
        }

        .btn-success:hover {
          background: #45a049;
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
          .admin-afiliaciones-page {
            padding: 1rem;
          }

          .table-container {
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

export default AdminAfiliaciones;
