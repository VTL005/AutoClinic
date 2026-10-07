import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { CarFront, ClipboardPlus, UserPlus } from 'lucide-react'
import { useMemo, useState } from 'react'
import { adminBookingService } from '../../services/adminBookingService'
import { adminMechanicService } from '../../services/adminMechanicService'
import { adminServiceCatalogService } from '../../services/adminServiceCatalogService'
import { adminUserService } from '../../services/adminUserService'
import { adminVehicleService } from '../../services/adminVehicleService'
import type { ErrorResponse } from '../../types/api'
import type { CreateVehicleRequest, Vehicle } from '../../types/vehicle'
import './AdminReceptionPage.css'

const errorText = (error: unknown) => axios.isAxiosError<ErrorResponse>(error)
  ? error.response?.data?.message || 'Không thể hoàn tất thao tác.'
  : 'Không thể hoàn tất thao tác.'

const initialVehicle: CreateVehicleRequest = {
  vin: '', licensePlate: '', manufacturer: '', model: '',
  manufactureYear: new Date().getFullYear(), color: '', engineType: '',
  fuelType: 'GASOLINE', transmissionType: 'AUTOMATIC', odometerKm: 0,
}

export default function AdminReceptionPage() {
  const queryClient = useQueryClient()
  const [customer, setCustomer] = useState({ fullName: '', phone: '', email: '' })
  const [customerId, setCustomerId] = useState('')
  const [vehicle, setVehicle] = useState<CreateVehicleRequest>(initialVehicle)
  const [vehicleId, setVehicleId] = useState('')
  const [mostRecentVehicle, setMostRecentVehicle] = useState<Vehicle | null>(null)
  const [booking, setBooking] = useState({ mechanicUserId: '', serviceId: '', customerNote: '', internalNote: '' })

  const activeCustomersQuery = useQuery({ queryKey: ['reception-customers', 'active'], queryFn: () => adminUserService.getUsers({ role: 'CUSTOMER', accountStatus: 'ACTIVE', page: 0, size: 100 }) })
  const pendingCustomersQuery = useQuery({ queryKey: ['reception-customers', 'pending-activation'], queryFn: () => adminUserService.getUsers({ role: 'CUSTOMER', accountStatus: 'PENDING_ACTIVATION', page: 0, size: 100 }) })
  const vehiclesQuery = useQuery({ queryKey: ['reception-vehicles'], queryFn: () => adminVehicleService.getVehicles({ page: 0, size: 100 }) })
  const mechanicsQuery = useQuery({ queryKey: ['reception-mechanics'], queryFn: () => adminMechanicService.getMechanics({ employmentStatus: 'ACTIVE', page: 0, size: 100 }) })
  const servicesQuery = useQuery({ queryKey: ['reception-services'], queryFn: () => adminServiceCatalogService.getServices({ active: 'true', page: 0, size: 100 }) })

  const customers = useMemo(
    () => [...(activeCustomersQuery.data?.content ?? []), ...(pendingCustomersQuery.data?.content ?? [])],
    [activeCustomersQuery.data, pendingCustomersQuery.data],
  )
  const customerVehicles = useMemo(() => {
    const savedVehicles = (vehiclesQuery.data?.content ?? []).filter(item => item.ownerUserId === Number(customerId))
    if (mostRecentVehicle?.ownerUserId === Number(customerId) && !savedVehicles.some(item => item.id === mostRecentVehicle.id)) {
      return [mostRecentVehicle, ...savedVehicles]
    }
    return savedVehicles
  }, [vehiclesQuery.data, customerId, mostRecentVehicle])
  const createCustomer = useMutation({
    mutationFn: () => adminUserService.createWalkInCustomer({ ...customer, email: customer.email || null }),
    onSuccess: value => { setCustomerId(String(value.id)); setVehicleId(''); setCustomer({ fullName: '', phone: '', email: '' }); void queryClient.invalidateQueries({ queryKey: ['reception-customers'] }); void queryClient.invalidateQueries({ queryKey: ['admin-customers'] }) },
  })
  const createVehicle = useMutation({
    mutationFn: () => adminVehicleService.createForCustomer(Number(customerId), vehicle),
    onSuccess: value => { setMostRecentVehicle(value); setVehicleId(String(value.id)); setVehicle(initialVehicle); void queryClient.invalidateQueries({ queryKey: ['reception-vehicles'] }); void queryClient.invalidateQueries({ queryKey: ['admin-vehicles'] }) },
  })
  const createBooking = useMutation({
    mutationFn: () => adminBookingService.createWalkInBooking({ customerUserId: Number(customerId), vehicleId: Number(vehicleId), mechanicUserId: Number(booking.mechanicUserId), serviceId: Number(booking.serviceId), customerNote: booking.customerNote || null, internalNote: booking.internalNote || null }),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['admin-bookings'] }),
  })
  const changeCustomer = (value: string) => { setCustomerId(value); setVehicleId('') }
  const loadingChoices = activeCustomersQuery.isLoading || pendingCustomersQuery.isLoading || vehiclesQuery.isLoading || mechanicsQuery.isLoading || servicesQuery.isLoading
  const choiceError = activeCustomersQuery.isError || pendingCustomersQuery.isError || vehiclesQuery.isError || mechanicsQuery.isError || servicesQuery.isError

  return <section className="reception-page">
    <header className="reception-heading"><span>QUẦY TIẾP NHẬN</span><h1>Tiếp nhận khách trực tiếp</h1><p>Tạo hồ sơ khách, khai báo phương tiện và lập lệnh sửa chữa trong một quy trình rõ ràng.</p></header>
    {choiceError && <div className="reception-alert">Không thể tải đầy đủ danh sách khách, xe, kỹ thuật viên hoặc dịch vụ. Hãy làm mới trang rồi thử lại.</div>}
    <div className="reception-grid">
      <form className="reception-card customer-card" onSubmit={event => { event.preventDefault(); createCustomer.mutate() }}>
        <div className="reception-card-heading"><span className="reception-step">1</span><div><h2><UserPlus size={20} />Khách vãng lai</h2><p>Tạo nhanh hồ sơ cho khách chưa có tài khoản.</p></div></div>
        <label>Họ tên<input required minLength={2} value={customer.fullName} onChange={e => setCustomer({ ...customer, fullName: e.target.value })} /></label>
        <label>Số điện thoại<input required value={customer.phone} onChange={e => setCustomer({ ...customer, phone: e.target.value })} /></label>
        <label>Email <small>không bắt buộc</small><input type="email" value={customer.email} onChange={e => setCustomer({ ...customer, email: e.target.value })} /></label>
        <button disabled={createCustomer.isPending}>{createCustomer.isPending ? 'Đang tạo hồ sơ…' : 'Lưu khách hàng'}</button>
        {createCustomer.isSuccess && <p className="reception-success">Đã tạo khách hàng. Bạn có thể chọn ngay ở bước 2.</p>}{createCustomer.isError && <p className="reception-error">{errorText(createCustomer.error)}</p>}
      </form>
      <form className="reception-card vehicle-card" onSubmit={event => { event.preventDefault(); createVehicle.mutate() }}>
        <div className="reception-card-heading"><span className="reception-step">2</span><div><h2><CarFront size={20} />Phương tiện</h2><p>Chọn khách trước, sau đó lưu xe vào hồ sơ khách.</p></div></div>
        <label>Khách hàng<select required value={customerId} onChange={e => changeCustomer(e.target.value)} disabled={activeCustomersQuery.isLoading || pendingCustomersQuery.isLoading}><option value="">{loadingChoices ? 'Đang tải dữ liệu…' : 'Chọn khách hàng'}</option>{customers.map(item => <option key={item.id} value={item.id}>{item.fullName} · {item.phone}{item.accountStatus === 'PENDING_ACTIVATION' ? ' · Chờ kích hoạt' : ''}</option>)}</select></label>
        <div className="reception-two-columns"><label>VIN<input required minLength={17} maxLength={17} value={vehicle.vin} onChange={e => setVehicle({ ...vehicle, vin: e.target.value.toUpperCase() })} /></label><label>Biển số<input required value={vehicle.licensePlate} onChange={e => setVehicle({ ...vehicle, licensePlate: e.target.value })} /></label></div>
        <div className="reception-two-columns"><label>Hãng<input required value={vehicle.manufacturer} onChange={e => setVehicle({ ...vehicle, manufacturer: e.target.value })} /></label><label>Mẫu xe<input required value={vehicle.model} onChange={e => setVehicle({ ...vehicle, model: e.target.value })} /></label></div>
        <button disabled={createVehicle.isPending || !customerId}>{createVehicle.isPending ? 'Đang thêm xe…' : 'Thêm phương tiện'}</button>
        {createVehicle.isSuccess && <p className="reception-success">Đã thêm xe. Xe này đã được chọn cho bước 3.</p>}{createVehicle.isError && <p className="reception-error">{errorText(createVehicle.error)}</p>}
      </form>
      <form className="reception-card booking-card" onSubmit={event => { event.preventDefault(); createBooking.mutate() }}>
        <div className="reception-card-heading"><span className="reception-step">3</span><div><h2><ClipboardPlus size={20} />Lập lịch tại quầy</h2><p>Chọn đúng đối tượng, kỹ thuật viên và hạng mục công việc.</p></div></div>
        <div className="reception-two-columns"><label>Khách hàng<select required value={customerId} onChange={e => changeCustomer(e.target.value)} disabled={activeCustomersQuery.isLoading || pendingCustomersQuery.isLoading}><option value="">Chọn khách hàng</option>{customers.map(item => <option key={item.id} value={item.id}>{item.fullName} · {item.phone}{item.accountStatus === 'PENDING_ACTIVATION' ? ' · Chờ kích hoạt' : ''}</option>)}</select></label><label>Phương tiện<select required value={vehicleId} onChange={e => setVehicleId(e.target.value)} disabled={!customerId || vehiclesQuery.isLoading || customerVehicles.length === 0}><option value="">{customerId ? (customerVehicles.length ? 'Chọn phương tiện' : 'Chưa có phương tiện') : 'Chọn khách hàng trước'}</option>{customerVehicles.map(item => <option key={item.id} value={item.id}>{item.manufacturer} {item.model} · {item.licensePlate}</option>)}</select></label></div>
        {customerId && !vehiclesQuery.isLoading && customerVehicles.length === 0 && <p className="reception-guidance">Khách này chưa có phương tiện. Hãy nhập thông tin xe ở bước 2, bấm <b>Thêm phương tiện</b>; xe mới sẽ tự được chọn tại đây.</p>}
        <div className="reception-two-columns"><label>Kỹ thuật viên<select required value={booking.mechanicUserId} onChange={e => setBooking({ ...booking, mechanicUserId: e.target.value })} disabled={mechanicsQuery.isLoading}><option value="">Chọn kỹ thuật viên</option>{(mechanicsQuery.data?.content ?? []).map(item => <option key={item.userId} value={item.userId}>{item.fullName}{item.specialization ? ` · ${item.specialization}` : ''}</option>)}</select></label><label>Dịch vụ<select required value={booking.serviceId} onChange={e => setBooking({ ...booking, serviceId: e.target.value })} disabled={servicesQuery.isLoading}><option value="">Chọn dịch vụ</option>{(servicesQuery.data?.content ?? []).map(item => <option key={item.id} value={item.id}>{item.name} · {item.durationMinutes} phút</option>)}</select></label></div>
        <label>Ghi chú khách<textarea placeholder="Triệu chứng xe, yêu cầu hoặc lưu ý cần tiếp nhận…" value={booking.customerNote} onChange={e => setBooking({ ...booking, customerNote: e.target.value })} /></label>
        <button disabled={createBooking.isPending || !customerId || !vehicleId || !booking.mechanicUserId || !booking.serviceId}>{createBooking.isPending ? 'Đang tiếp nhận…' : 'Tạo lịch sửa chữa'}</button>
        {createBooking.isSuccess && <p className="reception-success">Đã tiếp nhận lịch {createBooking.data.bookingCode}.</p>}{createBooking.isError && <p className="reception-error">{errorText(createBooking.error)}</p>}
      </form>
    </div>
  </section>
}
