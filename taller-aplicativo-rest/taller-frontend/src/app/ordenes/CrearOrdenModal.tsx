import React, { useEffect, useState } from 'react'
import { Dialog } from '@headlessui/react'

interface Orden {
  cliente_cedula: string
  placa: string
  fecha: string
  estado: string
  ciudad_id: number
  empleado_cedula: string
  form_pago_id: number
  detalles: {
    servicio_id?: number
    producto_id?: number
    cantidad: number
    precio: number
  }[]
}

export default function CrearOrdenModal({ onSuccess }: { onSuccess: () => void }) {
  const [isOpen, setIsOpen] = useState(false)
  const [form, setForm] = useState<Orden>({
    cliente_cedula: '',
    placa: '',
    fecha: new Date().toISOString(),
    estado: 'En Proceso',
    ciudad_id: 3,
    empleado_cedula: '0100000002',
    form_pago_id: 1,
    detalles: [
      { producto_id: 1, cantidad: 1, precio: 100.0 } // ejemplo por defecto
    ]
  })

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
  const { name, value } = e.target
  setForm({
    ...form,
    [name]: ['ciudad_id', 'form_pago_id'].includes(name) ? parseInt(value) : value
  })
}

  const handleCrear = async () => {
    try {
      const res = await fetch('http://localhost:5000/api/orden', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      })
      const data = await res.json()
      console.log('Orden creada:', data)
      onSuccess()
      setIsOpen(false)
    } catch (error) {
      console.error('Error al crear orden:', error)
    }
  }

  return (
    <>
      <button
        onClick={() => setIsOpen(true)}
        className="bg-green-600 text-white px-4 py-2 rounded font-semibold hover:bg-green-700 transition"
      >
        Crear Orden
      </button>

      <Dialog open={isOpen} onClose={() => setIsOpen(false)} className="fixed inset-0 z-50 flex items-center justify-center">
        <Dialog.Panel className="bg-white p-6 rounded-lg shadow-xl w-full max-w-md">
          <Dialog.Title className="text-2xl font-bold text-blue-700 mb-4">Nueva Orden</Dialog.Title>

          <div className="space-y-3">
            <input
              name="cliente_cedula"
              value={form.cliente_cedula}
              onChange={handleChange}
              placeholder="Cédula Cliente"
              className="border border-black p-2 w-full rounded text-black bg-white"
            />
            <input
              name="placa"
              value={form.placa}
              onChange={handleChange}
              placeholder="Placa"
              className="border border-black p-2 w-full rounded text-black bg-white"
            />
            <select
              name="estado"
              value={form.estado}
              onChange={handleChange}
              className="border border-black p-2 w-full rounded text-black bg-white"
            >
              <option value="En Proceso">En Proceso</option>
              <option value="Finalizada">Finalizada</option>
              <option value="Recibida">Recibida</option>
            </select>
            <select
              name="ciudad_id"
              value={form.ciudad_id}
              onChange={handleChange}
              className="border border-black p-2 w-full rounded text-black bg-white"
            >
              <option value={1}>Quito</option>
              <option value={2}>Guayaquil</option>
              <option value={3}>Cuenca</option>
            </select>
            <input
              name="empleado_cedula"
              value={form.empleado_cedula}
              onChange={handleChange}
              placeholder="Cédula del Empleado"
              className="border border-black p-2 w-full rounded text-black bg-white"
            />
            <select
              name="form_pago_id"
              value={form.form_pago_id}
              onChange={handleChange}
              className="border border-black p-2 w-full rounded text-black bg-white"
            >
              <option value={1}>Efectivo</option>
              <option value={2}>Crédito</option>
              <option value={3}>Transferencia</option>
            </select>
          </div>

          <div className="mt-6 flex justify-end gap-3">
            <button
              onClick={() => setIsOpen(false)}
              className="px-4 py-2 text-sm bg-gray-300 hover:bg-gray-400 text-black rounded font-semibold transition"
            >
              Cancelar
            </button>
            <button
              onClick={handleCrear}
              className="px-4 py-2 text-sm bg-blue-600 hover:bg-blue-700 text-white rounded font-semibold transition"
            >
              Guardar
            </button>
          </div>
        </Dialog.Panel>
      </Dialog>
    </>
  )
}
