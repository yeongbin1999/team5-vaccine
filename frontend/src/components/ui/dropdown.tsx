'use client';

import { useState, useRef, useEffect } from 'react';
import { ChevronDown } from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePathname } from 'next/navigation';

interface DropdownProps {
  trigger: React.ReactNode;
  children:
    | React.ReactNode
    | ((props: { onClose: () => void }) => React.ReactNode);
  className?: string;
  align?: 'left' | 'right';
}

export function Dropdown({
  trigger,
  children,
  className,
  align = 'right',
}: DropdownProps) {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const pathname = usePathname();

  useEffect(() => {
    setIsOpen(false);
  }, [pathname]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // children에 onClose prop을 내려줌
  const childrenWithOnClose =
    typeof children === 'function'
      ? (children as (props: { onClose: () => void }) => React.ReactNode)({
          onClose: () => setIsOpen(false),
        })
      : children;

  const renderDropdownContent = () => {
    if (!isOpen) return null;

    return (
      <div
        className={cn(
          'absolute top-full mt-1 w-fit border border-gray-200 rounded-lg shadow-lg py-2 w-[160px] z-50',
          align === 'right' ? 'right-0' : 'left-0',
          className
        )}
      >
        {childrenWithOnClose}
      </div>
    );
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center space-x-1 p-2 text-gray-600 hover:text-gray-900 transition-colors rounded-full"
      >
        {trigger}
        <ChevronDown
          className={cn(
            'w-4 h-4 transition-transform',
            isOpen ? 'rotate-180' : ''
          )}
        />
      </button>

      {renderDropdownContent()}
    </div>
  );
}

interface DropdownItemProps {
  children: React.ReactNode;
  onClick?: (
    e?: React.MouseEvent<HTMLButtonElement>,
    onClose?: () => void
  ) => void;
  className?: string;
  disabled?: boolean;
  onClose?: () => void;
}

export function DropdownItem({
  children,
  onClick,
  className,
  disabled,
  onClose,
}: DropdownItemProps) {
  return (
    <button
      onClick={e => {
        if (onClick) onClick(e, onClose);
        if (onClose) onClose();
      }}
      disabled={disabled}
      className={cn(
        'w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 transition-colors',
        disabled ? 'opacity-50 cursor-not-allowed' : '',
        className
      )}
    >
      {children}
    </button>
  );
}

export function DropdownSeparator() {
  return <div className="border-t border-gray-200 my-1" />;
}
