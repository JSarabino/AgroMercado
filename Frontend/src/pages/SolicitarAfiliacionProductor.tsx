import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Send, MapPin, User, Mail, Phone, FileText, Package } from 'lucide-react';
import afiliacionesService from '../services/afiliaciones.service';
import type { AfiliacionResumen } from '../services/afiliaciones.service';

const SolicitarAfiliacionProductor: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [zonasAprobadas, setZonasAprobadas] = useState<AfiliacionResumen[]>([]);
  const [cargandoZonas, setCargandoZonas] = useState(true);
  const [formData, setFormData] = useState({
    nombreProductor: '',
    documento: '',
    telefono: '',
    correo: '',
    zonaId: '',
    nombreZona: '',
    municipio: '',
    direccion: '',
    tipoProductos: '',
  });

  // Cargar zonas aprobadas al montar el componente
  useEffect(() => {
    const cargarZonas = async () => {
      try {
        setCargandoZonas(true);
        const zonas = await afiliacionesService.listarZonasAprobadas();
        setZonasAprobadas(zonas);
      } catch (error) {
        console.error('Error cargando zonas aprobadas:', error);
        alert('Error al cargar las zonas disponibles. Por favor recarga la página.');
      } finally {
        setCargandoZonas(false);
      }
    };

    cargarZonas();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Manejador especial para cuando se selecciona una zona
  const handleZonaChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const zonaSeleccionada = zonasAprobadas.find(
      (zona) => zona.afiliacionId === e.target.value
    );

    if (zonaSeleccionada) {
      setFormData(prev => ({
        ...prev,
        zonaId: zonaSeleccionada.zonaId || '',
        nombreZona: zonaSeleccionada.nombreVereda,
        municipio: zonaSeleccionada.municipio
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        zonaId: '',
        nombreZona: '',
        municipio: ''
      }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validaciones
    if (!formData.nombreProductor || !formData.documento || !formData.correo ||
        !formData.zonaId) {
      alert('Por favor completa todos los campos obligatorios, incluyendo la selección de zona');
      return;
    }

    try {
      setLoading(true);

      // Enviar solicitud real al backend
      const response = await afiliacionesService.solicitarAfiliacionProductor({
        zonaId: formData.zonaId,
        nombreProductor: formData.nombreProductor,
        documento: formData.documento,
        telefono: formData.telefono,
        correo: formData.correo,
        direccion: formData.direccion,
        tipoProductos: formData.tipoProductos,
      });

      alert(`✅ ${response.mensaje}\n\nID de Solicitud: ${response.afiliacionId}\nZona: ${formData.nombreZona} (${formData.municipio})\n\nPuedes ver el estado de tu solicitud en "Ver Mis Solicitudes".`);

      // Redirigir a ver solicitudes
      navigate('/productor/mis-solicitudes');
    } catch (error) {
      console.error('Error al solicitar afiliación:', error);
      const err = error as { response?: { data?: { message?: string; error?: string } } };
      const errorMessage = err.response?.data?.message || err.response?.data?.error || 'Error al enviar la solicitud';
      alert(`❌ ${errorMessage}\n\nPor favor intenta nuevamente.`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="solicitar-afiliacion-page">
      <div className="page-header">
        <button className="btn-back" onClick={() => navigate('/productor')}>
          <ArrowLeft size={20} />
          Volver al Dashboard
        </button>
        <h1>📝 Solicitar Afiliación a Zona Productiva sarabino</h1>
        <p>Completa el formulario para solicitar tu afiliación como productor</p>
      </div>

      <div className="form-container">
        <form onSubmit={handleSubmit} className="afiliacion-form">

          {/* Información Personal */}
          <div className="form-section">
            <h2>
              <User size={24} />
              Información Personal
            </h2>

            <div className="form-group">
              <label htmlFor="nombreProductor">
                Nombre Completo <span className="required">*</span>
              </label>
              <input
                type="text"
                id="nombreProductor"
                name="nombreProductor"
                value={formData.nombreProductor}
                onChange={handleChange}
                placeholder="Ej: Carlos Rodríguez"
                required
                disabled={loading}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="documento">
                  <FileText size={18} />
                  Documento de Identidad <span className="required">*</span>
                </label>
                <input
                  type="text"
                  id="documento"
                  name="documento"
                  value={formData.documento}
                  onChange={handleChange}
                  placeholder="CC-123456789"
                  required
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="telefono">
                  <Phone size={18} />
                  Teléfono
                </label>
                <input
                  type="tel"
                  id="telefono"
                  name="telefono"
                  value={formData.telefono}
                  onChange={handleChange}
                  placeholder="3001234567"
                  disabled={loading}
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="correo">
                <Mail size={18} />
                Correo Electrónico <span className="required">*</span>
              </label>
              <input
                type="email"
                id="correo"
                name="correo"
                value={formData.correo}
                onChange={handleChange}
                placeholder="productor@email.com"
                required
                disabled={loading}
              />
            </div>
          </div>

          {/* Información de la Zona */}
          <div className="form-section">
            <h2>
              <MapPin size={24} />
              Zona a la que deseas afiliarte
            </h2>

            {cargandoZonas ? (
              <div className="loading-message">
                <p>Cargando zonas disponibles...</p>
              </div>
            ) : zonasAprobadas.length === 0 ? (
              <div className="alert alert-warning">
                <strong>⚠️ No hay zonas disponibles</strong>
                <p>Actualmente no hay zonas aprobadas para afiliación. Por favor contacta al administrador.</p>
              </div>
            ) : (
              <>
                <div className="form-group">
                  <label htmlFor="zona-select">
                    Selecciona tu Zona/Vereda <span className="required">*</span>
                  </label>
                  <select
                    id="zona-select"
                    onChange={handleZonaChange}
                    value={zonasAprobadas.find(z => z.zonaId === formData.zonaId)?.afiliacionId || ''}
                    required
                    disabled={loading}
                    style={{
                      padding: '0.875rem',
                      border: '2px solid var(--border)',
                      borderRadius: '0.5rem',
                      fontSize: '1rem',
                      width: '100%',
                      cursor: 'pointer'
                    }}
                  >
                    <option value="">-- Selecciona una zona --</option>
                    {zonasAprobadas.map((zona) => (
                      <option key={zona.afiliacionId} value={zona.afiliacionId}>
                        {zona.nombreVereda} - {zona.municipio} (ID Zona: {zona.zonaId})
                      </option>
                    ))}
                  </select>
                  {formData.nombreZona && (
                    <p style={{ marginTop: '0.5rem', color: '#059669', fontSize: '0.875rem' }}>
                      ✓ Zona seleccionada: <strong>{formData.nombreZona}</strong> en {formData.municipio}
                    </p>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="direccion">
                    Dirección del Predio
                  </label>
                  <input
                    type="text"
                    id="direccion"
                    name="direccion"
                    value={formData.direccion}
                    onChange={handleChange}
                    placeholder="Vereda El Tablón, Finca La Esperanza"
                    disabled={loading}
                  />
                </div>
              </>
            )}
          </div>

          {/* Información Productiva */}
          <div className="form-section">
            <h2>
              <Package size={24} />
              Información de Producción
            </h2>

            <div className="form-group">
              <label htmlFor="tipoProductos">
                ¿Qué tipo de productos cultivas/produces?
              </label>
              <textarea
                id="tipoProductos"
                name="tipoProductos"
                value={formData.tipoProductos}
                onChange={handleChange}
                placeholder="Ej: Café orgánico, panela, frutas (lulo, mora)"
                disabled={loading}
                rows={3}
                style={{
                  padding: '0.875rem',
                  border: '2px solid var(--border)',
                  borderRadius: '0.5rem',
                  fontSize: '1rem',
                  fontFamily: 'inherit',
                  resize: 'vertical'
                }}
              />
            </div>
          </div>

          {/* Información adicional */}
          <div className="alert alert-info">
            <strong>ℹ️ Información importante:</strong>
            <ul>
              <li>Tu solicitud será revisada por el Administrador de la Zona seleccionada</li>
              <li>Una vez aprobada, podrás publicar tus productos en la plataforma</li>
              <li>Recibirás una notificación cuando se tome una decisión</li>
              <li>Los campos marcados con (*) son obligatorios</li>
            </ul>
          </div>

          {/* Botones de acción */}
          <div className="form-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={() => navigate('/productor')}
              disabled={loading}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={loading}
            >
              <Send size={20} />
              {loading ? 'Enviando...' : 'Enviar Solicitud'}
            </button>
          </div>
        </form>
      </div>

      <style>{`
        .loading-message {
          padding: 2rem;
          text-align: center;
          color: #718096;
          background: #f7fafc;
          border-radius: 8px;
          margin: 1rem 0;
        }

        .alert-warning {
          background: #fef3c7;
          border: 2px solid #fbbf24;
          border-radius: 8px;
          padding: 1.5rem;
          margin: 1rem 0;
        }

        .alert-warning strong {
          color: #92400e;
          display: block;
          margin-bottom: 0.5rem;
        }

        .alert-warning p {
          color: #92400e;
          margin: 0;
        }
      `}</style>
    </div>
  );
};

export default SolicitarAfiliacionProductor;
