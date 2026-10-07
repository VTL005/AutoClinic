import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import {
    ArrowLeft,
    CalendarClock,
    CheckCircle2,
    CircleAlert,
    Coffee,
    LoaderCircle,
    Save,
    UserCog,
} from 'lucide-react'
import { useMemo, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { adminMechanicScheduleService } from '../../services/adminMechanicScheduleService'
import { adminMechanicService } from '../../services/adminMechanicService'
import type { ErrorResponse } from '../../types/api'
import type { Mechanic } from '../../types/mechanic'
import './AdminOperationsPages.css'

const tomorrow = () => {
    const date = new Date()
    date.setDate(date.getDate() + 1)
    return date.toISOString().slice(0, 10)
}

const defaultShift = {
    workingDay: true,
    startTime: '08:00',
    endTime: '17:00',
    breakStart: '12:00',
    breakEnd: '13:00',
}

const getErrorMessage = (error: unknown, fallback: string) =>
    axios.isAxiosError<ErrorResponse>(error)
        ? error.response?.data?.message ?? fallback
        : fallback

const isNotFound = (error: unknown) =>
    axios.isAxiosError(error) && error.response?.status === 404

export default function AdminMechanicSchedulePage() {
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const [mechanicUserId, setMechanicUserId] = useState('')
    const [workDate, setWorkDate] = useState(tomorrow)
    const [editedShift, setEditedShift] = useState<typeof defaultShift | null>(null)
    const [notice, setNotice] = useState('')

    const mechanicsQuery = useQuery({
        queryKey: ['schedule-mechanics'],
        queryFn: () => adminMechanicService.getMechanics({
            employmentStatus: 'ACTIVE',
            page: 0,
            size: 100,
        }),
        staleTime: 30_000,
        retry: 1,
    })

    const mechanics = (mechanicsQuery.data?.content ?? []).filter(
        (mechanic) => mechanic.accountStatus === 'ACTIVE',
    )
    const selectedMechanic = useMemo<Mechanic | undefined>(
        () => mechanics.find((mechanic) => mechanic.userId === Number(mechanicUserId)),
        [mechanicUserId, mechanics],
    )

    const workDayQuery = useQuery({
        queryKey: ['mechanic-work-day', mechanicUserId, workDate],
        queryFn: () => adminMechanicScheduleService.getWorkDay(
            Number(mechanicUserId), workDate,
        ),
        enabled: Boolean(mechanicUserId && workDate),
        retry: false,
    })

    const workDaysQuery = useQuery({
        queryKey: ['mechanic-work-days', workDate],
        queryFn: () => adminMechanicScheduleService.getWorkDays(workDate),
        enabled: Boolean(workDate),
        retry: 1,
    })

    const workingMechanics = (workDaysQuery.data ?? []).filter((day) => day.workingDay)
    const savedShift = useMemo(() => {
        const day = workDayQuery.data
        if (!day) return defaultShift
        return {
            workingDay: day.workingDay,
            startTime: day.startTime.slice(0, 5),
            endTime: day.endTime.slice(0, 5),
            breakStart: day.breakStart?.slice(0, 5) ?? '',
            breakEnd: day.breakEnd?.slice(0, 5) ?? '',
        }
    }, [workDayQuery.data])
    const shift = editedShift ?? savedShift

    const updateShift = (changes: Partial<typeof defaultShift>) => {
        setEditedShift((current) => ({ ...(current ?? savedShift), ...changes }))
    }

    const saveMutation = useMutation({
        mutationFn: () => adminMechanicScheduleService.saveWorkDay(
            Number(mechanicUserId),
            workDate,
            {
                workingDay: shift.workingDay,
                startTime: shift.startTime,
                endTime: shift.endTime,
                breakStart: shift.breakStart || null,
                breakEnd: shift.breakEnd || null,
            },
        ),
        onSuccess: async () => {
            setNotice('Đã lưu ca làm. Khách hàng sẽ thấy các giờ trống của kỹ thuật viên này.')
            await queryClient.invalidateQueries({
                queryKey: ['mechanic-work-day', mechanicUserId, workDate],
            })
            await queryClient.invalidateQueries({
                queryKey: ['mechanic-work-days', workDate],
            })
        },
    })

    const submit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()
        setNotice('')
        saveMutation.reset()
        if (!mechanicUserId || !workDate) return
        saveMutation.mutate()
    }

    return (
        <div className="ops-page mechanic-schedule-page">
            <button type="button" className="ops-back" onClick={() => navigate('/admin')}>
                <ArrowLeft size={18} /> Tổng quan
            </button>

            <header className="ops-heading">
                <div className="ops-heading-copy">
                    <span>Booking Service</span>
                    <h1>Phân ca kỹ thuật viên</h1>
                    <p>Thiết lập ca trước để khách hàng chỉ nhìn thấy các khung giờ thực sự còn trống.</p>
                </div>
                <div className="ops-live-badge"><span /> Dữ liệu trực tiếp từ Booking Service</div>
            </header>

            <section className="ops-panel mechanic-schedule-panel">
                <div className="mechanic-schedule-guide">
                    <span><CalendarClock size={23} /></span>
                    <div>
                        <strong>Quy trình đặt lịch</strong>
                        <p>Chọn kỹ thuật viên và ngày làm việc, lưu ca. Sau đó khách hàng chọn kỹ thuật viên này để xem giờ trống; quản trị viên vẫn xác nhận phân công cuối cùng.</p>
                    </div>
                </div>

                {mechanicsQuery.isLoading ? (
                    <div className="ops-state"><LoaderCircle size={30} className="spin" /><strong>Đang tải kỹ thuật viên</strong></div>
                ) : mechanicsQuery.isError ? (
                    <div className="ops-state error"><CircleAlert size={30} /><strong>Không thể tải kỹ thuật viên</strong><span>{getErrorMessage(mechanicsQuery.error, 'Kiểm tra Identity Service rồi tải lại trang.')}</span></div>
                ) : mechanics.length === 0 ? (
                    <div className="ops-state empty"><UserCog size={30} /><strong>Chưa có kỹ thuật viên đang làm việc</strong><span>Hãy tạo kỹ thuật viên hoặc chuyển trạng thái hồ sơ sang “Đang làm việc” trước.</span></div>
                ) : (
                    <form className="mechanic-schedule-form" onSubmit={submit}>
                        <div className="ops-create-row">
                            <label>
                                Kỹ thuật viên *
                                <select value={mechanicUserId} onChange={(event) => { setMechanicUserId(event.target.value); setEditedShift(null); setNotice('') }} required>
                                    <option value="">Chọn kỹ thuật viên</option>
                                    {mechanics.map((mechanic) => <option key={mechanic.userId} value={mechanic.userId}>{mechanic.fullName} · {mechanic.specialization || mechanic.skillLevel}</option>)}
                                </select>
                            </label>
                            <label>
                                Ngày làm việc *
                                <input type="date" min={new Date().toISOString().slice(0, 10)} value={workDate} onChange={(event) => { setWorkDate(event.target.value); setEditedShift(null); setNotice('') }} required />
                            </label>
                        </div>

                        {selectedMechanic && <div className="mechanic-schedule-selected"><UserCog size={19} /><span><strong>{selectedMechanic.fullName}</strong><small>{selectedMechanic.specialization || 'Kỹ thuật viên tổng quát'} · ID {selectedMechanic.userId}</small></span></div>}

                        {mechanicUserId && workDayQuery.isLoading && <p className="ops-action-info"><LoaderCircle size={15} className="spin" /> Đang kiểm tra ca đã lưu...</p>}
                        {mechanicUserId && workDayQuery.isError && !isNotFound(workDayQuery.error) && <p className="ops-action-error">{getErrorMessage(workDayQuery.error, 'Không thể tải ca đã lưu.')}</p>}
                        {mechanicUserId && isNotFound(workDayQuery.error) && <p className="ops-action-info"><CalendarClock size={15} /> Ngày này chưa có ca; hãy thiết lập ca đầu tiên.</p>}

                        <label className="mechanic-working-toggle">
                            <input type="checkbox" checked={shift.workingDay} onChange={(event) => updateShift({ workingDay: event.target.checked })} />
                            <span><strong>Ngày làm việc</strong><small>Bỏ chọn nếu kỹ thuật viên nghỉ cả ngày.</small></span>
                        </label>

                        <div className="ops-shift-grid">
                            <label>Vào ca<input type="time" value={shift.startTime} onChange={(event) => updateShift({ startTime: event.target.value })} required /></label>
                            <label>Ra ca<input type="time" value={shift.endTime} onChange={(event) => updateShift({ endTime: event.target.value })} required /></label>
                            <label>Nghỉ từ<input type="time" value={shift.breakStart} onChange={(event) => updateShift({ breakStart: event.target.value })} /></label>
                            <label>Nghỉ đến<input type="time" value={shift.breakEnd} onChange={(event) => updateShift({ breakEnd: event.target.value })} /></label>
                        </div>
                        <p className="mechanic-schedule-note"><Coffee size={15} /> Ca phải nằm trong giờ mở cửa gara (08:00–17:00). Nếu có giờ nghỉ, hãy điền đủ cả hai mốc.</p>

                        {saveMutation.isError && <p className="ops-action-error">{getErrorMessage(saveMutation.error, 'Không thể lưu ca làm.')}</p>}
                        {notice && <p className="ops-action-success"><CheckCircle2 size={17} /> {notice}</p>}
                        <button type="submit" disabled={saveMutation.isPending || !mechanicUserId}>{saveMutation.isPending ? <><LoaderCircle size={17} className="spin" /> Đang lưu ca...</> : <><Save size={17} /> Lưu ca làm việc</>}</button>
                    </form>
                )}

                <section className="mechanic-shift-roster" aria-live="polite">
                    <div className="mechanic-shift-roster-heading">
                        <div>
                            <span>Danh sách ca đã lưu</span>
                            <h2>Kỹ thuật viên đang có ca ngày {workDate.split('-').reverse().join('/')}</h2>
                        </div>
                        <strong>{workingMechanics.length} người làm việc</strong>
                    </div>

                    {workDaysQuery.isLoading ? (
                        <p className="ops-action-info"><LoaderCircle size={15} className="spin" /> Đang tải danh sách ca làm...</p>
                    ) : workDaysQuery.isError ? (
                        <p className="ops-action-error">{getErrorMessage(workDaysQuery.error, 'Không thể tải danh sách ca làm.')}</p>
                    ) : workingMechanics.length === 0 ? (
                        <div className="mechanic-shift-empty"><CalendarClock size={21} /><span>Chưa có kỹ thuật viên nào được phân ca cho ngày này.</span></div>
                    ) : (
                        <div className="mechanic-shift-list">
                            {workingMechanics.map((day) => {
                                const mechanic = mechanics.find((item) => item.userId === day.mechanicUserId)
                                const breakTime = day.breakStart && day.breakEnd
                                    ? `${day.breakStart.slice(0, 5)}–${day.breakEnd.slice(0, 5)}`
                                    : 'Không có giờ nghỉ'

                                return (
                                    <article className="mechanic-shift-row" key={day.mechanicUserId}>
                                        <div className="mechanic-shift-person"><UserCog size={18} /><span><strong>{mechanic?.fullName ?? `Kỹ thuật viên #${day.mechanicUserId}`}</strong><small>{mechanic?.specialization || mechanic?.skillLevel || `ID ${day.mechanicUserId}`}</small></span></div>
                                        <div><small>Ca làm</small><strong>{day.startTime.slice(0, 5)}–{day.endTime.slice(0, 5)}</strong></div>
                                        <div><small>Giờ nghỉ</small><strong>{breakTime}</strong></div>
                                    </article>
                                )
                            })}
                        </div>
                    )}
                </section>
            </section>
        </div>
    )
}
