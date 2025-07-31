import './globals.css'
import React from 'react'
import { Inter } from 'next/font/google'
import { FaHome, FaUsers, FaCar, FaFileAlt, FaBox } from 'react-icons/fa'
import Link from 'next/link'

const inter = Inter({ subsets: ['latin'] })

export const metadata = {
  title: 'Taller Mecánico',
  description: 'Sistema de gestión de clientes, vehículos y órdenes',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="es">
      <body className={inter.className}>
        <div className="flex min-h-screen bg-gray-100">
          {/* Sidebar */}
          <aside className="w-64 bg-blue-900 text-white flex flex-col justify-between p-6">
            <div>
              <h1 className="text-2xl font-bold mb-6">Taller Mecánico</h1>
              <nav className="flex flex-col gap-3 text-sm">
                <Link href="/">
                  <div className="flex items-center gap-2 hover:text-blue-300">
                    <FaHome /> Inicio
                  </div>
                </Link>
                <Link href="/clientes">
                  <div className="flex items-center gap-2 hover:text-blue-300">
                    <FaUsers /> Clientes
                  </div>
                </Link>
                <Link href="/vehiculos">
                  <div className="flex items-center gap-2 hover:text-blue-300">
                    <FaCar /> Vehículos
                  </div>
                </Link>
                <Link href="/ordenes">
                  <div className="flex items-center gap-2 hover:text-blue-300">
                    <FaFileAlt /> Órdenes
                  </div>
                </Link>
                <Link href="/catalogo">
                  <div className="flex items-center gap-2 hover:text-blue-300">
                    <FaBox /> Catálogo
                  </div>
                </Link>
              </nav>
            </div>

            <footer className="text-xs text-blue-200 mt-6">
              © 2025 – Proyecto BDD
            </footer>
          </aside>

          {/* Main content */}
          <main className="flex-1 p-6">{children}</main>
        </div>
      </body>
    </html>
  )
}
