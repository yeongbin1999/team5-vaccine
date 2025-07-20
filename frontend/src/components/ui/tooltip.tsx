'use client';

import { useState } from 'react';
import { cn } from '@/lib/utils';
interface TooltipProps {
  children: React.ReactNode;
  content: string;
  className?: string;
}

export function Tooltip({ children, content, className }: TooltipProps) {
  const [isVisible, setIsVisible] = useState(false);

  const renderTooltip = () => {
    if (!isVisible) return null;

    return (
      <div
        className={cn(
          'absolute z-50 px-2 py-1 text-xs text-white bg-gray-900 rounded shadow-lg whitespace-nowrap top-0left-0 transform -translate-y-full -translate-x-1',
          className
        )}
      >
        {content}
        <div className="absolute top-full left-4 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900" />
      </div>
    );
  };

  return (
    <div
      className="relative inline-block w-full"
      onMouseEnter={() => setIsVisible(true)}
      onMouseLeave={() => setIsVisible(false)}
    >
      {children}
      {renderTooltip()}
    </div>
  );
}
