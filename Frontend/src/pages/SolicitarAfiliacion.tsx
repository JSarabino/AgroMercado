import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import afiliacionesService from '../services/afiliaciones.service';
import { ArrowLeft, Send, MapPin, User, Mail, Phone, FileText } from 'lucide-react';

const SolicitarAfiliacion: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    nombreVereda: '',
    municipio: '',
    telefono: '',
    correo: '',
    representanteNombre: '',
    representanteDocumento: '',
    representanteCorreo: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validaciones
    if (!formData.nombreVereda || !formData.municipio || !formData.representanteNombre ||
        !formData.representanteDocumento || !formData.representanteCorreo) {
      alert('Por favor completa todos los campos obligatorios');
      return;
    }

    try {
      setLoading(true);
      const response = await afiliacionesService.solicitarAfiliacion(formData);

      alert(`✅ Solicitud enviada exitosamente!\n\nID: ${response.afiliacionId}\nZona: ${response.zonaId || 'Pendiente de asignación'}\n\n${response.mensaje}`);

      // Redirigir a la página de ver mis solicitudes
      navigate('/admin/mis-solicitudes');
    } catch (error) {
      console.error('Error al solicitar afiliación:', error);
      alert('❌ Error al enviar la solicitud. Por favor intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="solicitar-afiliacion-page">
      <div className="page-header">
        <button className="btn-back" onClick={() => navigate('/admin')}>
          <ArrowLeft size={20} />
          Volver al Dashboard
        </button>
        <h1>📝 Solicitar Nueva Afiliación de Zona</h1>
        <p>Completa el formulario para solicitar la creación de una nueva zona productiva</p>
      </div>

      <div className="form-container">
        <form onSubmit={handleSubmit} className="afiliacion-form">

          {/* Información de la Zona */}
          <div className="form-section">
            <h2>
              <MapPin size={24} />
              Información de la Zona
            </h2>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="nombreVereda">
                  Nombre de la Vereda <span className="required">*</span>
                </label>
                <input
                  type="text"
                  id="nombreVereda"
                  name="nombreVereda"
                  value={formData.nombreVereda}
                  onChange={handleChange}
                  placeholder="Ej: El Mirador"
                  required
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="municipio">
                  Municipio <span className="required">*</span>
                </label>
                <input
                  type="text"
                  id="municipio"
                  name="municipio"
                  value={formData.municipio}
                  onChange={handleChange}
                  placeholder="Ej: Popayán"
                  required
                  disabled={loading}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="telefono">
                  <Phone size={18} />
                  Teléfono de Contacto
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

              <div className="form-group">
                <label htmlFor="correo">
                  <Mail size={18} />
                  Correo de Contacto
                </label>
                <input
                  type="email"
                  id="correo"
                  name="correo"
                  value={formData.correo}
                  onChange={handleChange}
                  placeholder="contacto@vereda.com"
                  disabled={loading}
                />
              </div>
            </div>
          </div>

          {/* Información del Representante */}
          <div className="form-section">
            <h2>
              <User size={24} />
              Datos del Representante
            </h2>

            <div className="form-group">
              <label htmlFor="representanteNombre">
                Nombre Completo <span className="required">*</span>
              </label>
              <input
                type="text"
                id="representanteNombre"
                name="representanteNombre"
                value={formData.representanteNombre}
                onChange={handleChange}
                placeholder="Ej: Ana Ruiz"
                required
                disabled={loading}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="representanteDocumento">
                  <FileText size={18} />
                  Documento de Identidad <span className="required">*</span>
                </label>
                <input
                  type="text"
                  id="representanteDocumento"
                  name="representanteDocumento"
                  value={formData.representanteDocumento}
                  onChange={handleChange}
                  placeholder="CC-112233"
                  required
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="representanteCorreo">
                  <Mail size={18} />
                  Correo del Representante <span className="required">*</span>
                </label>
                <input
                  type="email"
                  id="representanteCorreo"
                  name="representanteCorreo"
                  value={formData.representanteCorreo}
                  onChange={handleChange}
                  placeholder="ana.ruiz@vereda.com"
                  required
                  disabled={loading}
                />
              </div>
            </div>
          </div>

          {/* Información adicional */}
          <div className="alert alert-info">
            <strong>ℹ️ Información importante:</strong>
            <ul>
              <li>Tu solicitud será revisada por un Administrador Global</li>
              <li>Recibirás una notificación una vez se tome una decisión</li>
              <li>Los campos marcados con (*) son obligatorios</li>
            </ul>
          </div>

          {/* Botones de acción */}
          <div className="form-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={() => navigate('/admin')}
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
    </div>
  );
};

export default SolicitarAfiliacion;
