'use client';

import React, { useState } from 'react';
import { createCliente } from '@/services/clienteService';
import { FaTimes } from 'react-icons/fa';

interface Props {
  onClose: () => void;
  onCreated: () => void;
   ciudadIdSeleccionada: number
}

export default function ClienteFormModal({ onClose, onCreated, ciudadIdSeleccionada }: Props) {
  const [form, setForm] = useState({
    cedula: '',
    nombre: '',
    telefono: '',
    correo: '',
    ciudad_id: ciudadIdSeleccionada,
  });

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.name === 'ciudad_id' ? Number(e.target.value) : e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const { cedula, nombre, ciudad_id } = form;

    if (!cedula || !nombre || !ciudad_id) {
      setError('Cédula, nombre y ciudad son obligatorios');
      return;
    }

    setLoading(true);
    const result = await createCliente(form);
    setLoading(false);

    if (result.success) {
      onCreated();
      onClose();
    } else {
      setError(result.message || 'Error al crear cliente');
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center">
      <div className="bg-white p-6 rounded-lg w-full max-w-md shadow-lg relative">
        <button className="absolute top-2 right-2 text-gray-500 hover:text-red-500" onClick={onClose}>
          <FaTimes />
        </button>
        <h3 className="text-xl font-semibold mb-4 text-blue-800">Nuevo Cliente</h3>

        {error && <div className="bg-red-100 text-red-700 p-2 rounded mb-3">{error}</div>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="text"
            name="cedula"
            placeholder="Cédula"
            className="w-full border rounded p-2 text-gray-900"
            value={form.cedula}
            onChange={handleChange}
          />
          <input
            type="text"
            name="nombre"
            placeholder="Nombre"
            className="w-full border rounded p-2 text-gray-900"
            value={form.nombre}
            onChange={handleChange}
          />
          <input
            type="text"
            name="telefono"
            placeholder="Teléfono"
            className="w-full border rounded p-2 text-gray-900"
            value={form.telefono}
            onChange={handleChange}
          />
          <input
            type="email"
            name="correo"
            placeholder="Correo"
            className="w-full border rounded p-2 text-gray-900"
            value={form.correo}
            onChange={handleChange}
          />
          <select
            name="ciudad_id"
            value={form.ciudad_id}
            onChange={handleChange}
            className="w-full border rounded p-2 text-gray-900"
          >
            <option value={1}>Quito</option>
            <option value={2}>Guayaquil</option>
            <option value={3}>Cuenca</option>
          </select>

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
  );
}