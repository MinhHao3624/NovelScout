import { useEffect, useId, useRef, useState } from 'react'

export default function FilterSelect({ label, value, options, onChange }) {
  const [open, setOpen] = useState(false)
  const [highlighted, setHighlighted] = useState(0)
  const rootRef = useRef(null)
  const listId = useId()
  const selectedIndex = Math.max(0, options.findIndex((option) => option.value === value))
  const selected = options[selectedIndex]

  useEffect(() => {
    if (!open) return undefined

    function closeOnOutsideClick(event) {
      if (!rootRef.current?.contains(event.target)) setOpen(false)
    }

    function closeOnEscape(event) {
      if (event.key === 'Escape') setOpen(false)
    }

    document.addEventListener('pointerdown', closeOnOutsideClick)
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('pointerdown', closeOnOutsideClick)
      document.removeEventListener('keydown', closeOnEscape)
    }
  }, [open])

  function toggle() {
    if (!open) setHighlighted(selectedIndex)
    setOpen((current) => !current)
  }

  function select(option) {
    onChange(option.value)
    setOpen(false)
  }

  function handleKeyDown(event) {
    if (!['ArrowDown', 'ArrowUp', 'Enter', ' '].includes(event.key)) return
    event.preventDefault()

    if (!open) {
      setHighlighted(selectedIndex)
      setOpen(true)
      return
    }
    if (event.key === 'ArrowDown') setHighlighted((current) => (current + 1) % options.length)
    if (event.key === 'ArrowUp') setHighlighted((current) => (current - 1 + options.length) % options.length)
    if (event.key === 'Enter' || event.key === ' ') select(options[highlighted])
  }

  return (
    <div className={`filter-select ${open ? 'open' : ''}`} ref={rootRef}>
      <button className="filter-select-trigger" type="button" aria-label={label} aria-haspopup="listbox"
        aria-expanded={open} aria-controls={listId} onClick={toggle} onKeyDown={handleKeyDown}>
        <span>{selected.label}</span><span className="filter-select-caret" aria-hidden="true" />
      </button>
      {open && (
        <div className="filter-select-menu" id={listId} role="listbox" aria-label={label}>
          <p>{label}</p>
          {options.map((option, index) => (
            <button className={`${option.value === value ? 'selected' : ''} ${index === highlighted ? 'highlighted' : ''}`}
              type="button" role="option" aria-selected={option.value === value} key={option.value || 'all'}
              onPointerEnter={() => setHighlighted(index)} onClick={() => select(option)}>
              <span>{option.label}</span><span className="filter-option-check" aria-hidden="true">✓</span>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
