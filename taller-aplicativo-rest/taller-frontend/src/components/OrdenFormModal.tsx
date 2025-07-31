// components/OrdenFormModal.tsx
'use client'

import React, { useState, useEffect } from 'react'
import { FaTimes } from 'react-icons/fa'

interface Props {
  onClose: () => void
  onSubmit: (formData: any) => void
  initialData?: any
  title: string
}

export default function OrdenFormModal({ onClose, onSubmit, initialData = {}, title }: Props) {
  const [form, setForm] = useState({
    fecha: '',
    proveedor: '',
    total: '',
    estado: '',
    cliente_cedula: '',
    ciudad_id: ''
  })

  const [error, setError] = useState('')

  useEffect(() => {
    if (initialData) {
      setForm({ ...form, ...initialData })
    }
  }, [initialData])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const { fecha, proveedor, total, estado, cliente_cedula, ciudad_id } = form
    if (!fecha || !proveedor || !total || !estado || !cliente_cedula || !ciudad_id) {
      return setError('Todos los campos son obligatorios.')
    }
    setError('')
    onSubmit(form)
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md relative">
        <button className="absolute top-2 right-2 text-gray-500 hover:text-red-600" onClick={onClose}>
          <FaTimes />
        </button>
        <h3 className="text-xl font-bold text-blue-800 mb-4">{title}</h3>
        {error && <div className="text-red-600 mb-2 text-sm">{error}</div>}
        <form onSubmit={handleSubmit} className="space-y-3">
          <input type="datetime-local" name="fecha" value={form.fecha} onChange={handleChange} className="w-full p-2 border rounded" />
          <input name="proveedor" placeholder="Proveedor" value={form.proveedor} onChange={handleChange} className="w-full p-2 border rounded" />
          <input name="total" type="number" placeholder="Total" value={form.total} onChange={handleChange} className="w-full p-2 border rounded" />
          <select name="estado" value={form.estado} onChange={handleChange} className="w-full p-2 border rounded">
            <option value="">Seleccione Estado</option>
            <option value="En Proceso">En Proceso</option>
            <option value="Recibida">Recibida</option>
            <option value="Finalizada">Finalizada</option>
          </select>
          <input name="cliente_cedula" placeholder="Cédula del Cliente" value={form.cliente_cedula} onChange={handleChange} className="w-full p-2 border rounded" />
          <input name="ciudad_id" type="number" placeholder="ID Ciudad" value={form.ciudad_id} onChange={handleChange} className="w-full p-2 border rounded" />
          <button type="submit" className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">Guardar</button>
        </form>
      </div>
    </div>
  )
}
