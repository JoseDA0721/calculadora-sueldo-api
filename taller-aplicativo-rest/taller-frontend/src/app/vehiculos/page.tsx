'use client'

import React, { useEffect, useState } from 'react'
import { FaPlus, FaEdit, FaTrash } from 'react-icons/fa'
import VehiculoFormModal from '@/components/VehiculoFormModal'
import { deleteVehiculo } from '@/services/vehiculoService';


interface Vehiculo {
  placa: string
  marca: string
  modelo: string
  tipo: string
  cliente_cedula: string
}

export default function VehiculosPage() {
  const [vehiculos, setVehiculos] = useState<Vehiculo[]>([])
  const [showModal, setShowModal] = useState(false)
  const [placaBusqueda, setPlacaBusqueda] = useState('');
const [vehiculoBuscado, setVehiculoBuscado] = useState<Vehiculo | null>(null);
const [editableVehiculo, setEditableVehiculo] = useState<Vehiculo | null>(null);


  const handleDelete = async (placa: string) => {
    const confirmar = window.confirm('¿Estás seguro de eliminar este vehículo?');
    if (!confirmar) return;

    const result = await deleteVehiculo(placa);
    if (result.success) {
      setVehiculos(vehiculos.filter(v => v.placa !== placa));
    } else {
      alert(result.message || 'Error al eliminar vehículo');
    }
  };

  const buscarVehiculoPorPlaca = async () => {
  try {
    const res = await fetch(`http://localhost:5000/api/vehiculo/${placaBusqueda}`);
    const data = await res.json();

    if (Array.isArray(data) && data.length > 0) {
      setVehiculoBuscado(data[0]); // ✅ accede al primer objeto del array
    } else {
      alert('Vehículo no encontrado');
      setVehiculoBuscado(null);
    }
  } catch (error) {
    console.error('Error al buscar vehículo:', error);
    alert('No se pudo realizar la búsqueda');
  }
};


const actualizarVehiculo = async () => {
  if (!editableVehiculo) return;

  try {
    const tipo_id =
      editableVehiculo.tipo.toLowerCase() === 'sedán'
        ? 1
        : editableVehiculo.tipo.toLowerCase() === 'suv'
        ? 2
        : 3;

    const res = await fetch(`http://localhost:5000/api/vehiculo/${editableVehiculo.placa}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        marca: editableVehiculo.marca,
        modelo: editableVehiculo.modelo,
        tipo_id,
      }),
    });

    if (!res.ok) throw new Error('Error al actualizar');
    alert('Vehículo actualizado correctamente');
    setEditableVehiculo(null);
  } catch (error) {
    console.error('Error actualizando vehículo:', error);
    alert('No se pudo actualizar');
  }
};



  useEffect(() => {
    async function fetchData() {
      try {
        const res = await fetch('http://localhost:5000/api/vehiculos')
        const data = await res.json()
        console.log('Vehículos recibidos:', data)

        if (Array.isArray(data)) {
          const tipos = {
            1: 'Sedán',
            2: 'SUV',
            3: 'Pickup'
          }

          const vehiculosTransformados = data.map((vehiculo: any) => ({
            ...vehiculo,
            tipo: tipos[vehiculo.tipo_id as keyof typeof tipos] || 'Desconocido'
          }))


          setVehiculos(vehiculosTransformados)
        } else {
          setVehiculos([])
        }
      } catch (error) {
        console.error('Error al obtener vehículos:', error)
        setVehiculos([])
      }

    }
    fetchData()
  }, [])

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h2 className="text-3xl font-bold text-[#001A30]">Listado de Vehículos (Global)</h2>
        <button
          className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition"
          onClick={() => setShowModal(true)}
        >
          <FaPlus />
          Nuevo Vehículo
        </button>

      </div>
<div className="mb-4 flex gap-2">
  <input
    type="text"
    placeholder="Buscar por placa"
    value={placaBusqueda}
    onChange={(e) => setPlacaBusqueda(e.target.value)}
    className="border border-gray-300 p-2 rounded text-sm text-gray-900 bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
  />
  <button
    onClick={buscarVehiculoPorPlaca}
    className="bg-blue-600 text-white px-3 py-1 rounded"
  >
    Buscar
  </button>
</div>
{vehiculoBuscado && (
  <div className="border rounded p-4 bg-gray-50 mt-4 space-y-2">
    <h3 className="font-bold text-[#001A30]">Resultado de búsqueda:</h3>
    <table className="w-full text-sm border mt-2">
      <thead className="bg-blue-100 text-blue-800">
        <tr>
          <th className="p-2">Placa</th>
          <th className="p-2">Marca</th>
          <th className="p-2">Modelo</th>
          <th className="p-2">Tipo</th>
          <th className="p-2">Cédula Dueño</th>
          <th className="p-2">Acciones</th>
        </tr>
      </thead>
      <tbody>
        <tr className="border-t hover:bg-gray-50">
          <td className="p-2 text-gray-900">{vehiculoBuscado.placa}</td>
          <td className="p-2 text-gray-900">{vehiculoBuscado.marca}</td>
          <td className="p-2 text-gray-900">{vehiculoBuscado.modelo}</td>
          <td className="p-2 text-gray-900">{vehiculoBuscado.tipo}</td>
          <td className="p-2 text-gray-900">{vehiculoBuscado.cliente_cedula}</td>
          <td className="p-2">
            <button
              className="text-green-600 hover:text-green-800"
              onClick={() => setEditableVehiculo(vehiculoBuscado)}
            >
              <FaEdit />
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

)}


      <div className="overflow-x-auto rounded-lg shadow bg-white">
        {editableVehiculo && (
  <div className="border rounded p-4 bg-gray-50 mt-2 space-y-3">
    <h3 className="font-bold text-[#001A30]">Editar Vehículo</h3>
    <input
      type="text"
      value={editableVehiculo.marca}
      onChange={(e) =>
        setEditableVehiculo({ ...editableVehiculo, marca: e.target.value })
      }
      placeholder="Marca"
      className="border p-2 rounded w-full"
    />
    <input
      type="text"
      value={editableVehiculo.modelo}
      onChange={(e) =>
        setEditableVehiculo({ ...editableVehiculo, modelo: e.target.value })
      }
      placeholder="Modelo"
      className="border p-2 rounded w-full"
    />
    <input
      type="text"
      value={editableVehiculo.tipo}
      onChange={(e) =>
        setEditableVehiculo({ ...editableVehiculo, tipo: e.target.value })
      }
      placeholder="Tipo"
      className="border p-2 rounded w-full"
    />
    <button
      onClick={actualizarVehiculo}
      className="bg-green-600 text-white px-4 py-2 rounded"
    >
      Guardar Cambios
    </button>
  </div>
)}

        <table className="min-w-full text-sm">
          <thead className="bg-blue-100 text-blue-800">
            <tr>
              <th className="p-3 text-left">Placa</th>
              <th className="p-3 text-left">Marca</th>
              <th className="p-3 text-left">Modelo</th>
              <th className="p-3 text-left">Tipo</th>
              <th className="p-3 text-left">Cédula Dueño</th>
              <th className="p-3 text-left">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {vehiculos.map((vehiculo) => (
              <tr key={vehiculo.placa} className="border-b hover:bg-gray-50">
                <td className="p-3 text-gray-900 font-medium">{vehiculo.placa}</td>
                <td className="p-3 text-gray-900">{vehiculo.marca}</td>
                <td className="p-3 text-gray-900">{vehiculo.modelo}</td>
                <td className="p-3 text-gray-900">{vehiculo.tipo}</td>
                <td className="p-3 text-gray-900">{vehiculo.cliente_cedula}</td>
                <td className="p-3 flex gap-2">
                  <button className="text-blue-600 hover:text-blue-800" title="Editar">
                    <FaEdit />
                  </button>
                  <button className="text-red-600 hover:text-red-800"
                    title="Eliminar"
                    onClick={() => handleDelete(vehiculo.placa)}>
                    <FaTrash />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {showModal && (
          <VehiculoFormModal
            onClose={() => setShowModal(false)}
            onCreated={async () => {
              setShowModal(false)
              try {
                const res = await fetch('http://localhost:5000/api/vehiculos')
                const data = await res.json()
                if (Array.isArray(data)) {
                  setVehiculos(data)
                } else {
                  setVehiculos([])
                }
              } catch (error) {
                console.error('Error al refrescar vehículos:', error)
                setVehiculos([])
              }
            }}
          />
        )}

      </div>
    </div>
  )
}
