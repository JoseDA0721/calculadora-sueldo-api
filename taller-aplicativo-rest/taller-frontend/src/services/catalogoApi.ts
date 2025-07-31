// src/services/catalogoApi.ts
export async function fetchServicios() {
  const res = await fetch('http://localhost:5000/api/servicios')
  return res.json()
}

export async function fetchCiudades() {
  const res = await fetch('http://localhost:5000/api/ciudades')
  return res.json()
}

export async function fetchTiposVehiculo() {
  const res = await fetch('http://localhost:5000/api/tipos-vehiculo')
  return res.json()
}

export async function fetchProductos() {
  const res = await fetch('http://localhost:5000/api/productos')
  return res.json()
}
