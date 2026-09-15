import { useAuth } from "react-oidc-context";
import { useState, useEffect } from "react";
import './App.css';

function App() {
  const auth = useAuth();
  const [pedidos, setPedidos] = useState([]);
  const [errorApi, setErrorApi] = useState("");
  
  const [descripcion, setDescripcion] = useState("");
  const [fechaInicio, setFechaInicio] = useState("");
  const [fechaTermino, setFechaTermino] = useState("");
  const [comentarios, setComentarios] = useState({});

  const [editandoId, setEditandoId] = useState(null);
  const [editDescripcion, setEditDescripcion] = useState("");
  const [editInicio, setEditInicio] = useState("");
  const [editTermino, setEditTermino] = useState("");

  const obtenerRoles = () => {
    if (!auth.user?.access_token) return { isSolicitante: false, isAprobador: false };
    try {
      const payload = JSON.parse(atob(auth.user.access_token.split('.')[1]));
      let groups = payload["cognito:groups"] || [];
      if (typeof groups === 'string') groups = [groups];

      return {
        isSolicitante: groups.includes('solicitante'),
        isAprobador: groups.includes('aprobador')
      };
    } catch (e) {
      return { isSolicitante: false, isAprobador: false };
    }
  };

  const { isSolicitante, isAprobador } = obtenerRoles();

  const cargarPedidos = async () => {
    try {
      setErrorApi("");
      const respuesta = await fetch("http://localhost:8080/api/datos", {
        headers: { Authorization: `Bearer ${auth.user?.access_token}` }
      });
      if (respuesta.ok) {
        const data = await respuesta.json();
        setPedidos(data);
      } else {
        setErrorApi(`Error al cargar: ${respuesta.status}`);
      }
    } catch (error) {
      setErrorApi("No se pudo conectar con el backend.");
    }
  };

  useEffect(() => {
    if (auth.isAuthenticated) {
      cargarPedidos();
    }
  }, [auth.isAuthenticated]);

  const crearPedido = async (e) => {
    e.preventDefault();
    try {
      const respuesta = await fetch("http://localhost:8080/api/datos", {
        method: "POST",
        headers: { 
          "Content-Type": "application/json",
          Authorization: `Bearer ${auth.user?.access_token}` 
        },
        body: JSON.stringify({ 
          descripcion, 
          fechaInicio: fechaInicio || null, 
          fechaTermino: fechaTermino || null 
        })
      });
      if (respuesta.ok) {
        setDescripcion("");
        setFechaInicio("");
        setFechaTermino("");
        cargarPedidos();
      }
    } catch (error) {
      console.error("Error al crear:", error);
    }
  };

  const revisarPedido = async (id, estado) => {
    const comentario = comentarios[id] || "";
    try {
      const respuesta = await fetch(`http://localhost:8080/api/datos/${id}/revision`, {
        method: "PATCH",
        headers: { 
          "Content-Type": "application/json",
          Authorization: `Bearer ${auth.user?.access_token}` 
        },
        body: JSON.stringify({ estado: estado, comentarioAprobador: comentario })
      });
      if (respuesta.ok) {
        setComentarios({ ...comentarios, [id]: "" });
        cargarPedidos();
      }
    } catch (error) {
      console.error("Error al revisar:", error);
    }
  };

  const eliminarPedido = async (id) => {
    if (!window.confirm("¿Estás seguro de eliminar esta solicitud?")) return;
    try {
      const respuesta = await fetch(`http://localhost:8080/api/datos/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${auth.user?.access_token}` }
      });
      if (respuesta.ok) {
        cargarPedidos();
      }
    } catch (error) {
      console.error("Error al eliminar:", error);
    }
  };

  const guardarEdicion = async (id) => {
    try {
      const respuesta = await fetch(`http://localhost:8080/api/datos/${id}`, {
        method: "PUT",
        headers: { 
          "Content-Type": "application/json",
          Authorization: `Bearer ${auth.user?.access_token}` 
        },
        body: JSON.stringify({ 
          descripcion: editDescripcion, 
          fechaInicio: editInicio || null, 
          fechaTermino: editTermino || null 
        })
      });
      if (respuesta.ok) {
        setEditandoId(null);
        cargarPedidos();
      }
    } catch (error) {
      console.error("Error al editar:", error);
    }
  };

  if (auth.isLoading) return <div className="loader">Cargando entorno seguro...</div>;
  if (auth.error) return <div className="error-msg">Error de autenticación: {auth.error.message}</div>;

  if (auth.isAuthenticated) {
    return (
      <div className="app-container">
        <header className="app-header">
          <div>
            <h2 className="app-title">🏢 Pedidos360</h2>
            <p className="app-subtitle">Portal Corporativo de Gestión y Aprobaciones</p>
          </div>
          <div className="user-box">
            <span className="user-email">{auth.user?.profile.email}</span>
            <button onClick={() => auth.signoutRedirect()} className="btn-logout">
              Cerrar Sesión
            </button>
          </div>
        </header>

        {errorApi && <div className="alert-error">{errorApi}</div>}

        {/* Solo el rol solicitante puede crear peticiones */}
        {isSolicitante && !isAprobador && (
          <section className="section-card">
            <h3 className="section-title">📅 Ingresar Solicitud de Vacaciones o Días Administrativos</h3>
            <form onSubmit={crearPedido} className="form-grid">
              <input 
                type="text" placeholder="Motivo (Ej: Vacaciones de Verano)" required
                value={descripcion} onChange={(e) => setDescripcion(e.target.value)}
                className="input-field"
              />
              <div style={{ display: 'flex', gap: '5px', flex: 1 }}>
                <input 
                  type="date" title="Fecha Inicio" required
                  value={fechaInicio} onChange={(e) => setFechaInicio(e.target.value)}
                  className="input-field"
                />
                <input 
                  type="date" title="Fecha Término" required
                  value={fechaTermino} onChange={(e) => setFechaTermino(e.target.value)}
                  className="input-field"
                />
              </div>
              <button type="submit" className="btn-primary">Crear Solicitud</button>
            </form>
          </section>
        )}

        <section className="section-card">
          <h3 className="section-title">📋 Estado General de Solicitudes</h3>
          <div className="table-container">
            <table className="styled-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Descripción</th>
                  <th>Período (Desde - Hasta)</th>
                  <th>Estado</th>
                  <th>Acciones / Gestión</th>
                </tr>
              </thead>
              <tbody>
                {pedidos.map((p) => (
                  <tr key={p.id}>
                    <td>#{p.id}</td>
                    <td>
                      {editandoId === p.id ? (
                        <input 
                          type="text" value={editDescripcion} 
                          onChange={(e) => setEditDescripcion(e.target.value)}
                          className="input-small"
                        />
                      ) : (
                        p.descripcion
                      )}
                    </td>
                    <td>
                      {editandoId === p.id ? (
                        <div style={{display:'flex', gap:'4px'}}>
                          <input type="date" value={editInicio} onChange={(e) => setEditInicio(e.target.value)} className="input-small" />
                          <input type="date" value={editTermino} onChange={(e) => setEditTermino(e.target.value)} className="input-small" />
                        </div>
                      ) : (
                        `${p.fechaInicio || 'N/A'} al ${p.fechaTermino || 'N/A'}`
                      )}
                    </td>
                    <td>
                      <span className={`badge ${
                        p.estado === 'PENDIENTE' ? 'badge-pendiente' : p.estado === 'APROBADO' ? 'badge-aprobado' : 'badge-rechazado'
                      }`}>
                        {p.estado}
                      </span>
                      {p.comentarioAprobador && <div className="nota">Nota: {p.comentarioAprobador}</div>}
                    </td>
                    <td>
                      <div className="action-group">
                        {/* Acciones de Jefatura para el Aprobador */}
                        {isAprobador && p.estado === 'PENDIENTE' && (
                          <div className="aprobador-box">
                            <input 
                              type="text" placeholder="Comentario opcional" 
                              value={comentarios[p.id] || ""} 
                              onChange={(e) => setComentarios({ ...comentarios, [p.id]: e.target.value })}
                              className="input-small"
                            />
                            <div className="btn-row">
                              <button onClick={() => revisarPedido(p.id, 'APROBADO')} className="btn-approve">Aprobar</button>
                              <button onClick={() => revisarPedido(p.id, 'RECHAZADO')} className="btn-reject">Rechazar</button>
                            </div>
                          </div>
                        )}

                        {/* Controles CRUD para el Solicitante */}
                        {isSolicitante && p.estado === 'PENDIENTE' && (
                          <div className="btn-row">
                            {editandoId === p.id ? (
                              <>
                                <button onClick={() => guardarEdicion(p.id)} className="btn-save">Guardar</button>
                                <button onClick={() => setEditandoId(null)} className="btn-cancel">Cancelar</button>
                              </>
                            ) : (
                              <>
                                <button onClick={() => { setEditandoId(p.id); setEditDescripcion(p.descripcion); setEditInicio(p.fechaInicio); setEditTermino(p.fechaTermino); }} className="btn-edit">Editar</button>
                                <button onClick={() => eliminarPedido(p.id)} className="btn-delete">Eliminar</button>
                              </>
                            )}
                          </div>
                        )}

                        {p.estado !== 'PENDIENTE' && <span className="text-muted">Finalizado</span>}
                      </div>
                    </td>
                  </tr>
                ))}
                {pedidos.length === 0 && (
                  <tr><td colSpan="5" className="no-data">No hay solicitudes registradas en el sistema.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    );
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <h1 className="login-title">Pedidos360</h1>
        <p className="login-text">Plataforma Cloud-Native de Gestión y Aprobaciones Corporativas.</p>
        <button onClick={() => auth.signinRedirect()} className="btn-login-big">
          Iniciar Sesión en el Portal
        </button>
      </div>
    </div>
  );
}

export default App;