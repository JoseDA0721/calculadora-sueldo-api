'use client'

import React, { useState } from 'react'
import { FaTimes } from 'react-icons/fa'
import { createVehiculo } from '@/services/vehiculoService';


interface Props {
  onClose: () => void
  onCreated: () => void
}

export default function VehiculoFormModal({ onClose, onCreated }: Props) {
  const [form, setForm] = useState({
    placa: '',
    marca: '',
    modelo: '',
    tipo_id: '',
    cliente_cedula: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setLoading(true);
  setError('');

  const result = await createVehiculo({
    ...form,
    tipo_id: Number(form.tipo_id),
  });

  setLoading(false);

  if (result.success) {
    onCreated();
    onClose();
  } else {
    setError(result.message || 'Error al registrar vehículo');
  }
};

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md relative">
        <button onClick={onClose} className="absolute top-2 right-2 text-gray-500 hover:text-red-500">
          <FaTimes />
        </button>
        <h3 className="text-lg font-bold text-blue-800 mb-4">Registrar Vehículo</h3>

        {error && <p className="bg-red-100 text-red-700 px-3 py-2 rounded mb-3">{error}</p>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            name="placa"
            placeholder="Placa"
            className="w-full border p-2 rounded text-gray-900"
            value={form.placa}
            onChange={handleChange}
          />
          <input
            name="marca"
            placeholder="Marca"
            className="w-full border p-2 rounded text-gray-900"
            value={form.marca}
            onChange={handleChange}
          />
          <input
            name="modelo"
            placeholder="Modelo"
            className="w-full border p-2 rounded text-gray-900"
            value={form.modelo}
            onChange={handleChange}
          />
          <input
            name="tipo_id"
            placeholder="ID del Tipo (ej. 1)"
            className="w-full border p-2 rounded text-gray-900"
            value={form.tipo_id}
            onChange={handleChange}
          />

          <input
            name="cliente_cedula"
            placeholder="Cédula del Dueño"
            className="w-full border p-2 rounded text-gray-900"
            value={form.cliente_cedula}
            onChange={handleChange}
          />

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 transition"
          >
            {loading ? 'Guardando...' : 'Guardar'}
          </button>
        </form>
        
      </div>
    </div>
  )
}
