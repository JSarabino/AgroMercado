import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import afiliacionesService from '../services/afiliaciones.service';
import type { SolicitudProductorZona } from '../services/afiliaciones.service';
import { ArrowLeft, MapPin, Calendar, CheckCircle, FileText, Building2, Mail, Phone, Map } from 'lucide-react';

interface ZonaConInfo extends SolicitudProductorZona {
  nombreVereda?: string;
  municipio?: string;
  representanteNombre?: string;
  telefonoZona?: string;
  correoZona?: string;
}

const MisZonasAfiliadas: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [zonasAfiliadas, setZonasAfiliadas] = useState<ZonaConInfo[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarZonasAfiliadas();
  }, [user]);

  const cargarZonasAfiliadas = async () => {
    if (!user?.id) {
      console.error('No hay usuario autenticado');
      setLoading(false);
      return;
    }

    try {
      const solicitudes = await afiliacionesService.listarMisZonasAfiliadas(user.id);

      // Enriquecer cada solicitud con información de la zona
      const zonasEnriquecidas: ZonaConInfo[] = await Promise.all(
        solicitudes.map(async (solicitud) => {
          try {
            // Obtener información de la zona desde las afiliaciones
            const afiliacionesZona = await afiliacionesService.listarAfiliacionesZona(solicitud.zonaId);
            const afiliacionZona = afiliacionesZona.find(a => a.zonaId === solicitud.zonaId);

            return {
              ...solicitud,
              nombreVereda: afiliacionZona?.nombreVereda,
              municipio: afiliacionZona?.municipio,
              representanteNombre: afiliacionZona?.representanteNombre,
              telefonoZona: afiliacionZona?.telefono,
              correoZona: afiliacionZona?.correo,
            };
          } catch (error) {
            console.error(`Error obteniendo info de zona ${solicitud.zonaId}:`, error);
            return solicitud as ZonaConInfo;
          }
        })
      );

      setZonasAfiliadas(zonasEnriquecidas);
    } catch (error) {
      console.error('Error cargando zonas afiliadas:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner"></div>
        <p>Cargando tus zonas...</p>
      </div>
    );
  }

  return (
    <div className="mis-zonas-page">
      <div className="page-header">
        <button className="btn-back" onClick={() => navigate('/productor')}>
          <ArrowLeft size={20} />
          Volver al Dashboard
        </button>
        <h1>🌾 Mis Zonas Productivas</h1>
        <p>Zonas a las que estás afiliado como productor</p>
      </div>

      {zonasAfiliadas.length === 0 ? (
        <div className="empty-state">
          <MapPin size={64} color="#cbd5e0" />
          <h2>No estás afiliado a ninguna zona</h2>
          <p>Aún no tienes ninguna afiliación aprobada a zonas productivas</p>
          <button
            className="btn-primary"
            onClick={() => navigate('/productor/solicitar-afiliacion')}
          >
            <MapPin size={20} />
            Solicitar Afiliación
          </button>
        </div>
      ) : (
        <>
          <div style={{ marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <p style={{ fontSize: '1.125rem', color: '#4a5568' }}>
                Estás afiliado a <strong>{zonasAfiliadas.length}</strong> zona{zonasAfiliadas.length > 1 ? 's' : ''} productiva{zonasAfiliadas.length > 1 ? 's' : ''}
              </p>
            </div>
            <button
              className="btn-secondary"
              onClick={() => navigate('/productor/solicitar-afiliacion')}
            >
              <MapPin size={20} />
              Solicitar Otra Afiliación
            </button>
          </div>

          <div className="zonas-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
            {zonasAfiliadas.map((zona) => (
              <div key={zona.solicitudId || zona.id} className="zona-card" style={{
                background: 'white',
                border: '1px solid #e2e8f0',
                borderRadius: '0.75rem',
                padding: '1.5rem',
                boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
                transition: 'all 0.2s',
              }}>
                <div style={{ display: 'flex', alignItems: 'start', justifyContent: 'space-between', marginBottom: '1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <div style={{
                      width: '48px',
                      height: '48px',
                      borderRadius: '50%',
                      background: '#d1fae5',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center'
                    }}>
                      <MapPin size={24} color="#10b981" />
                    </div>
                    <div>
                      <h3 style={{ margin: 0, fontSize: '1.25rem', color: '#1a202c', fontWeight: 600 }}>
                        {zona.nombreVereda || zona.zonaId || 'Zona Productiva'}
                      </h3>
                      {zona.municipio && (
                        <p style={{ margin: '0.25rem 0 0 0', fontSize: '0.875rem', color: '#718096' }}>
                          <Map size={14} style={{ display: 'inline', marginRight: '0.25rem', verticalAlign: 'middle' }} />
                          {zona.municipio}
                        </p>
                      )}
                    </div>
                  </div>
                  <span style={{
                    background: '#d1fae5',
                    color: '#065f46',
                    padding: '0.25rem 0.75rem',
                    borderRadius: '9999px',
                    fontSize: '0.875rem',
                    fontWeight: 600,
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.25rem'
                  }}>
                    <CheckCircle size={14} />
                    APROBADA
                  </span>
                </div>

                <div style={{ marginTop: '1rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {zona.representanteNombre && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#4a5568', fontSize: '0.875rem' }}>
                      <Building2 size={16} style={{ color: '#10b981', flexShrink: 0 }} />
                      <span><strong>Representante:</strong> {zona.representanteNombre}</span>
                    </div>
                  )}

                  {zona.telefonoZona && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#4a5568', fontSize: '0.875rem' }}>
                      <Phone size={16} style={{ color: '#10b981', flexShrink: 0 }} />
                      <span>{zona.telefonoZona}</span>
                    </div>
                  )}

                  {zona.correoZona && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#4a5568', fontSize: '0.875rem' }}>
                      <Mail size={16} style={{ color: '#10b981', flexShrink: 0 }} />
                      <span>{zona.correoZona}</span>
                    </div>
                  )}

                  {zona.zonaId && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#718096', fontSize: '0.75rem', marginTop: '0.5rem', paddingTop: '0.75rem', borderTop: '1px solid #e2e8f0' }}>
                      <FileText size={14} />
                      <span><strong>ID Zona:</strong> <code style={{ background: '#f7fafc', padding: '0.2rem 0.4rem', borderRadius: '4px', fontSize: '0.75rem' }}>{zona.zonaId}</code></span>
                    </div>
                  )}

                  {zona.createdAt && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#718096', fontSize: '0.875rem' }}>
                      <Calendar size={16} style={{ color: '#718096', flexShrink: 0 }} />
                      <span><strong>Afiliado desde:</strong> {new Date(zona.createdAt).toLocaleDateString('es-CO', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                      })}</span>
                    </div>
                  )}

                  {zona.fechaDecision && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#718096', fontSize: '0.875rem' }}>
                      <CheckCircle size={16} style={{ color: '#10b981', flexShrink: 0 }} />
                      <span><strong>Aprobada el:</strong> {new Date(zona.fechaDecision).toLocaleDateString('es-CO', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                      })}</span>
                    </div>
                  )}

                  {zona.observaciones && (
                    <div style={{ marginTop: '0.5rem', padding: '0.75rem', background: '#f7fafc', borderRadius: '0.5rem', fontSize: '0.875rem', color: '#4a5568' }}>
                      <strong>Observaciones:</strong> {zona.observaciones}
                    </div>
                  )}
                </div>

                <div style={{ marginTop: '1.5rem', paddingTop: '1rem', borderTop: '1px solid #e2e8f0' }}>
                  <button
                    className="btn-primary"
                    style={{ width: '100%' }}
                    onClick={() => navigate('/productor')}
                  >
                    Publicar Productos en esta Zona
                  </button>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default MisZonasAfiliadas;
