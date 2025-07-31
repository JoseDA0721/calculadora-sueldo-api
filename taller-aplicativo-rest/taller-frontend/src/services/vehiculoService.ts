const BASE_URL = 'http://localhost:5000/api';

export async function getVehiculos() {
  try {
    const res = await fetch(`${BASE_URL}/vehiculos`);
    if (!res.ok) throw new Error('Error al obtener vehículos');
    return await res.json();
  } catch (error) {
    console.error(error);
    return [];
  }
}

export async function createVehiculo(vehiculo: any) {
  try {
    const res = await fetch(`${BASE_URL}/vehiculo`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(vehiculo),
    });

    if (!res.ok) {
      const error = await res.json();
      return { success: false, message: error.message };
    }

    return { success: true };
  } catch (error) {
    return { success: false, message: 'Error de conexión' };
  }
}

export async function deleteVehiculo(placa: string) {
  try {
    const res = await fetch(`http://localhost:5000/api/vehiculo/${placa}`, {
      method: 'DELETE'
    });
    if (!res.ok) {
      const error = await res.json();
      return { success: false, message: error.message };
    }
    return { success: true };
  } catch (error) {
    return { success: false, message: 'Error de conexión' };
  }
}