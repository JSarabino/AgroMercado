import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import usuariosService from '../services/usuarios.service';
import afiliacionesService from '../services/afiliaciones.service';
import type { AfiliacionResumen } from '../services/afiliaciones.service';
import { Users, CheckCircle, UserPlus, Mail, XCircle } from 'lucide-react';

interface SolicitudProductor {
  afiliacionId: string;
  usuarioId: string;
  nombre: string;
  email: string;
  telefono?: string;
  documento?: string;
  direccion?: string;
  tipoProductos?: string;
  fechaSolicitud: string;
  estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';
}

const AdminProductores: React.FC = () => {
  const { user } = useAuth();
  const [solicitudes, setSolicitudes] = useState<SolicitudProductor[]>([]);
  const [loading, setLoading] = useState(true);
  const [zonaId, setZonaId] = useState<string | null>(null);
  const [filter, setFilter] = useState<'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'TODAS'>('PENDIENTE');
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    obtenerZonaYcargarSolicitudes();
  }, [user]);

  // Obtener la zonaId del admin_zona desde sus afiliaciones aprobadas
  const obtenerZonaYcargarSolicitudes = async () => {
    if (!user?.id) {
      setLoading(false);
      return;
    }

    try {
      setLoading(true);

      // Obtener las afiliaciones del admin_zona para encontrar su zona aprobada
      const misAfiliaciones = await afiliacionesService.listarMisSolicitudes(user.id);
      const zonaAprobada = misAfiliaciones.find(af => af.estado === 'APROBADA' && af.zonaId);

      if (!zonaAprobada || !zonaAprobada.zonaId) {
        alert('No tienes una zona asignada. Debes tener una solicitud aprobada primero.');
        setLoading(false);
        return;
      }

      setZonaId(zonaAprobada.zonaId);

      // Cargar solicitudes de productores para esta zona
      await cargarSolicitudes(zonaAprobada.zonaId);
    } catch (error) {
      console.error('Error obteniendo zona:', error);
      alert('Error al cargar la información de tu zona. Por favor recarga la página.');
    } finally {
      setLoading(false);
    }
  };

  const cargarSolicitudes = async (zonaIdParam: string) => {
    try {
      // Obtener las solicitudes de productores para esta zona usando el nuevo endpoint
      const solicitudesRaw = await afiliacionesService.listarSolicitudesProductorZona(zonaIdParam);

      // Mapear a la estructura esperada por el componente
      const solicitudesProductores: SolicitudProductor[] = solicitudesRaw.map((sol) => ({
        afiliacionId: sol.solicitudId || sol.afiliacionId,
        usuarioId: sol.productorUsuarioId || sol.usuarioId,
        nombre: sol.nombreProductor || 'Productor',
        email: sol.correo || 'N/A',
        telefono: sol.telefono,
        documento: sol.documento,
        direccion: sol.direccion,
        tipoProductos: sol.tipoProductos,
        fechaSolicitud: sol.createdAt || sol.updatedAt,
        estado: sol.estado,
        }));

      setSolicitudes(solicitudesProductores);
    } catch (error) {
      console.error('Error cargando solicitudes:', error);
      alert('Error al cargar las solicitudes de productores.');
    }
  };

  const handleAprobar = async (afiliacionId: string, usuarioId: string) => {
    if (!confirm('¿Aprobar este productor en tu zona? Esto otorgará automáticamente la membresía de PRODUCTOR.')) return;

    if (!zonaId) {
      alert('Error: No se pudo obtener la zona asignada.');
      return;
    }

    try {
      setProcessing(true);

      // Aprobar la solicitud del productor (el backend otorga la membresía automáticamente)
      await afiliacionesService.aprobarProductor(afiliacionId, 'Productor aprobado por administrador zonal');

      alert('✅ Productor aprobado exitosamente y membresía otorgada automáticamente');

      // Recargar solicitudes
      if (zonaId) {
        await cargarSolicitudes(zonaId);
      }
    } catch (error) {
      console.error('Error aprobando:', error);
      const err = error as { response?: { data?: { message?: string; error?: string } } };
      const errorMessage = err.response?.data?.message || err.response?.data?.error || 'Error al aprobar';
      alert(`❌ ${errorMessage}`);
    } finally {
      setProcessing(false);
    }
  };

  const handleRechazar = async (afiliacionId: string) => {
    const observaciones = prompt('Ingrese las observaciones del rechazo (opcional):');
    if (observaciones === null) return; // Usuario canceló

    if (!confirm('¿Rechazar esta solicitud de productor?')) return;

    try {
      setProcessing(true);
      await afiliacionesService.rechazarProductor(afiliacionId, observaciones || 'Solicitud rechazada por administrador zonal');

      alert('❌ Solicitud rechazada');

      // Recargar solicitudes
      if (zonaId) {
        await cargarSolicitudes(zonaId);
      }
    } catch (error) {
      console.error('Error rechazando:', error);
      alert('❌ Error al rechazar la solicitud');
    } finally {
      setProcessing(false);
    }
  };

  const solicitudesFiltradas = filter === 'TODAS'
    ? solicitudes
    : solicitudes.filter(s => s.estado === filter);

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner"></div>
        <p>Cargando...</p>
      </div>
    );
  }

  return (
    <div className="admin-productores-page">
      <div className="page-header">
        <div>
          <h1>👨‍🌾 Gestión de Productores</h1>
          <p>Administre las solicitudes de productores</p>
        </div>
      </div>

      <div className="filters-bar">
        <button className={filter === 'TODAS' ? 'active' : ''} onClick={() => setFilter('TODAS')}>
          Todas
        </button>
        <button className={filter === 'PENDIENTE' ? 'active' : ''} onClick={() => setFilter('PENDIENTE')}>
          <UserPlus size={16} />
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

      {solicitudesFiltradas.length === 0 ? (
        <div className="empty-state">
          <Users size={64} color="#cbd5e0" />
          <h2>No hay solicitudes de productores</h2>
          <p>No hay solicitudes de productores {filter !== 'TODAS' && `en estado ${filter}`} para tu zona.</p>
        </div>
      ) : (
        <div className="solicitudes-list">
          {solicitudesFiltradas.map(solicitud => (
            <div key={solicitud.afiliacionId} className="solicitud-card">
              <div className="card-left">
                <div className="user-avatar">
                  <Users size={32} />
                </div>
                <div className="user-info">
                  <h3>{solicitud.nombre}</h3>
                  <div className="contact-info">
                    <span>
                      <Mail size={14} />
                      ID: {solicitud.usuarioId}
                    </span>
                    <span style={{ marginLeft: '1rem', fontSize: '0.875rem', color: '#718096' }}>
                      Solicitud: {solicitud.afiliacionId}
                    </span>
                  </div>
                  <div style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: '#718096' }}>
                    Fecha: {new Date(solicitud.fechaSolicitud).toLocaleDateString('es-CO')}
                  </div>
                  <div style={{
                    marginTop: '0.5rem',
                    padding: '0.25rem 0.75rem',
                    borderRadius: '12px',
                    display: 'inline-block',
                    fontSize: '0.75rem',
                    fontWeight: 600,
                    backgroundColor: solicitud.estado === 'PENDIENTE' ? '#fef3c7' :
                                     solicitud.estado === 'APROBADA' ? '#d1fae5' : '#fee2e2',
                    color: solicitud.estado === 'PENDIENTE' ? '#92400e' :
                           solicitud.estado === 'APROBADA' ? '#065f46' : '#991b1b',
                  }}>
                    {solicitud.estado}
                  </div>
                </div>
              </div>

              {solicitud.estado === 'PENDIENTE' && (
                <div className="card-right" style={{ display: 'flex', gap: '0.5rem' }}>
                  <button
                    className="btn-danger btn-sm"
                    onClick={() => handleRechazar(solicitud.afiliacionId)}
                    disabled={processing}
                  >
                    <XCircle size={16} />
                    Rechazar
                  </button>
                  <button
                    className="btn-success btn-sm"
                    onClick={() => handleAprobar(solicitud.afiliacionId, solicitud.usuarioId)}
                    disabled={processing}
                  >
                    <CheckCircle size={16} />
                    Aprobar
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AdminProductores;
