import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'
import { AuthProvider } from "react-oidc-context";

const cognitoAuthConfig = {
  authority: `https://cognito-idp.us-east-1.amazonaws.com/${import.meta.env.VITE_COGNITO_USER_POOL_ID}`, 
  client_id: import.meta.env.VITE_COGNITO_CLIENT_ID, 
  
  redirect_uri: "http://localhost:4200/",
  response_type: "code",
  scope: "email openid profile pedidos/api.read pedidos/api.write pedidos/api.update pedidos/api.delete",
};

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AuthProvider {...cognitoAuthConfig}>
      <App />
    </AuthProvider>
  </React.StrictMode>,
)