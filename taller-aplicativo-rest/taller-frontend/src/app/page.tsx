'use client'

import Image from "next/image"
import React, { useEffect, useState } from 'react'
import { fetchDashboardStats } from "@/services/dashboardService"

export default function Home() {
  const [stats, setStats] = useState({
    totalClientes: 0,
    totalVehiculos: 0,
    totalOrdenes: 0,
  })

  useEffect(() => {
    fetchDashboardStats().then(setStats)
  }, [])

  const [fechaSync, setFechaSync] = useState('')

useEffect(() => {
  fetchDashboardStats().then(setStats)
  setFechaSync(new Date().toLocaleString())
}, [])


  return (
    <div
      className="min-h-screen bg-gray-100 bg-cover bg-center flex flex-col justify-start items-center pt-12 px-6 pb-10"
      style={{ backgroundImage: 'url("/fondo-taller.jpg")' }}
    >
      <main className="flex flex-col gap-[32px] items-center sm:items-start w-full max-w-5xl">
        <h1 className="text-4xl font-extrabold text-white drop-shadow-lg">¡Bienvenido!</h1>

        {/* DASHBOARD VISUAL */}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6 w-full">
          <div className="bg-white rounded-lg shadow p-4 flex items-center gap-4">
            <span className="text-3xl text-purple-700">👥</span>
            <div>
              <p className="text-sm text-gray-600">Clientes</p>
              <p className="text-2xl font-bold text-blue-900">{stats.totalClientes}</p>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-4 flex items-center gap-4">
            <span className="text-3xl text-red-600">🚗</span>
            <div>
              <p className="text-sm text-gray-600">Vehículos</p>
              <p className="text-2xl font-bold text-blue-900">{stats.totalVehiculos}</p>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-4 flex items-center gap-4">
            <span className="text-3xl text-indigo-600">📄</span>
            <div>
              <p className="text-sm text-gray-600">Órdenes</p>
              <p className="text-2xl font-bold text-blue-900">{stats.totalOrdenes}</p>
            </div>
          </div>
        </div>

        {/* Estado del sistema */}
        <div className="bg-white rounded-lg shadow p-6 w-full mt-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-3">Estado del sistema</h2>
          <p className="text-gray-700 flex items-center gap-2 mb-1">
            ⏰ Última sincronización: <strong>{fechaSync}</strong>
          </p>
          <p className="text-gray-700 flex items-center gap-2">
            🌐 Nodo activo: <strong>Guayaquil</strong>
          </p>
        </div>
      </main>

      {/* Footer */}
      <footer className="mt-12 flex gap-[24px] flex-wrap items-center justify-center">
        <a className="flex items-center gap-2 hover:underline hover:underline-offset-4"
          href="https://nextjs.org/learn" target="_blank" rel="noopener noreferrer">
          <Image aria-hidden src="/file.svg" alt="File icon" width={16} height={16} /> Learn
        </a>
        <a className="flex items-center gap-2 hover:underline hover:underline-offset-4"
          href="https://vercel.com/templates" target="_blank" rel="noopener noreferrer">
          <Image aria-hidden src="/window.svg" alt="Window icon" width={16} height={16} /> Examples
        </a>
        <a className="flex items-center gap-2 hover:underline hover:underline-offset-4"
          href="https://nextjs.org" target="_blank" rel="noopener noreferrer">
          <Image aria-hidden src="/globe.svg" alt="Globe icon" width={16} height={16} /> Go to nextjs.org →
        </a>
      </footer>
    </div>
  )
}
