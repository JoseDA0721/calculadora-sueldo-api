'use client'

import React, { useEffect, useState } from 'react'
import { getClientes } from '@/services/clienteService'
import { FaPlus, FaEdit, FaTrash } from 'react-icons/fa'
import ClienteFormModal from '@/components/ClienteFormModal'
import { deleteCliente } from '@/services/clienteService'


interface Cliente {
  cedula: string
  nombre: string
  telefono?: string
  correo?: string
  ciudad_id?: number
}

const ciudades = [
  { id: 1, nombre: 'Quito' },
  { id: 2, nombre: 'Guayaquil' },
  { id: 3, nombre: 'Cuenca' },
]

export default function ClientesPage() {
  const [showModal, setShowModal] = useState(false)
  const [clientes, setClientes] = useState<Cliente[]>([])
  const [ciudad, setCiudad] = useState<string | undefined>('quito')
const [busquedaCedula, setBusquedaCedula] = useState('')
const [clienteBuscado, setClienteBuscado] = useState<Cliente | null>(null)

  const handleDelete = async (cedula: string) => {
    const confirmed = window.confirm('¿Estás seguro de eliminar este cliente?');
    if (!confirmed) return;

    const result = await deleteCliente(cedula);
    if (result.success) {
      alert('Cliente eliminado correctamente');
      const updated = await getClientes(ciudad);
      setClientes(updated);
    } else {
      alert('Error al eliminar el cliente: ' + result.message);
    }
  };

  const buscarClientePorCedula = async () => {
  try {
    const res = await fetch(`http://localhost:5000/api/cliente/${busquedaCedula}`)
    const data = await res.json()
    if (data && data.cedula) {
      setClienteBuscado(data)
    } else {
      alert('Cliente no encontrado')
      setClienteBuscado(null)
    }
  } catch (error) {
    console.error('Error al buscar cliente:', error)
    alert('No se pudo encontrar el cliente')
  }
}


const actualizarCliente = async () => {
   if (!clienteBuscado) return;
  try {
    const res = await fetch(`http://localhost:5000/api/cliente/${clienteBuscado.cedula}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: clienteBuscado.nombre,
        telefono: clienteBuscado.telefono,
        correo: clienteBuscado.correo
      })
    })
    if (!res.ok) throw new Error('Error en actualización')
    alert('Cliente actualizado correctamente')
    setClienteBuscado(null) // limpiar el formulario
     // 🔄 Actualiza tabla visual
    const updated = await getClientes(ciudad)
    setClientes(updated)
    setClienteBuscado(null)
  } catch (error) {
    console.error('Error actualizando cliente:', error)
    alert('Error al actualizar cliente')
  }
}


  useEffect(() => {
    async function fetchData() {
      const data = await getClientes(ciudad)
      setClientes(data)
    }
    fetchData()
  }, [ciudad])

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h2 className="text-3xl font-bold text-[#001A30]">Listado de Clientes (Global)</h2>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition"
        >
          <FaPlus />
          Nuevo Cliente
        </button>
      </div>

      <div className="flex items-center gap-2">
        <label className="text-sm font-medium text-[#001A30]">Ciudad:</label>
        <select
          value={ciudad}
          onChange={(e) => setCiudad(e.target.value)}
          className="border border-gray-300 rounded px-3 py-2 text-sm text-gray-900 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          {ciudades.map((c) => (
            <option key={c.id} value={c.nombre.toLowerCase()}>
              {c.nombre}
            </option>
          ))}
        </select>
      </div>
<div className="mb-4 flex gap-2">
  <input
    type="text"
    placeholder="Buscar por cédula"
    value={busquedaCedula}
    onChange={(e) => setBusquedaCedula(e.target.value)}
    className="border border-gray-300 p-2 rounded text-sm text-gray-900 bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
  />
  <button
    onClick={buscarClientePorCedula}
    className="bg-blue-600 text-white px-3 py-1 rounded"
  >
    Buscar
  </button>
</div>
{clienteBuscado && (
  <div className="border rounded p-4 bg-gray-100 mt-2 space-y-3 shadow-md">
    <h3 className="font-bold text-[#001A30] mb-2">Editar Cliente</h3>
    <input
      type="text"
      value={clienteBuscado.nombre}
      onChange={(e) =>
        setClienteBuscado({ ...clienteBuscado, nombre: e.target.value })
      }
      placeholder="Nombre"
      className="border border-gray-300 p-2 rounded w-full text-gray-900 bg-white"
    />
    <input
      type="text"
      value={clienteBuscado.telefono}
      onChange={(e) =>
        setClienteBuscado({ ...clienteBuscado, telefono: e.target.value })
      }
      placeholder="Teléfono"
      className="border border-gray-300 p-2 rounded w-full text-gray-900 bg-white"
    />
    <input
      type="email"
      value={clienteBuscado.correo}
      onChange={(e) =>
        setClienteBuscado({ ...clienteBuscado, correo: e.target.value })
      }
      placeholder="Correo"
      className="border border-gray-300 p-2 rounded w-full text-gray-900 bg-white"
    />
    <button
      onClick={actualizarCliente}
      className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition"
    >
      Guardar Cambios
    </button>
  </div>
)}

      <div className="overflow-x-auto rounded-lg shadow bg-white">
        <table className="min-w-full text-sm">
          <thead className="bg-blue-100 text-blue-800">
            <tr>
              <th className="p-3 text-left">Cédula</th>
              <th className="p-3 text-left">Nombre</th>
              <th className="p-3 text-left">Correo</th>
              <th className="p-3 text-left">Teléfono</th>
              <th className="p-3 text-left">Ciudad</th>
              <th className="p-3 text-left">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {clientes.map((cliente) => (
              <tr key={cliente.cedula} className="border-b hover:bg-gray-50">
                <td className="p-3 text-gray-900 font-medium">{cliente.cedula}</td>
                <td className="p-3 text-gray-900">{cliente.nombre}</td>
                <td className="p-3 text-gray-900">{cliente.correo || '-'}</td>
                <td className="p-3 text-gray-900">{cliente.telefono || '-'}</td>
                <td className="p-3">
                  <span className="px-2 py-1 text-xs rounded bg-gray-300 text-gray-800 font-semibold">
                    {cliente.ciudad_id === 1
                      ? 'Quito'
                      : cliente.ciudad_id === 2
                      ? 'Guayaquil'
                      : cliente.ciudad_id === 3
                      ? 'Cuenca'
                      : '-'}
                  </span>
                </td>
                <td className="p-3 flex gap-2">
                  <button
  className="text-blue-600 hover:text-blue-800"
  title="Editar"
  onClick={() => setClienteBuscado(cliente)}
>
  <FaEdit />
</button>

                  <button className="text-red-600 hover:text-red-800"
                    title="Eliminar"
                    onClick={() => handleDelete(cliente.cedula)}>
                    <FaTrash />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal de creación */}
      {showModal && (
        <ClienteFormModal
        ciudadIdSeleccionada={
    ciudades.find((c) => c.nombre.toLowerCase() === ciudad)?.id || 1
  }
          onClose={() => setShowModal(false)}
          onCreated={() => getClientes(ciudad).then(setClientes)}
        />
      )}

    </div>
  )
}