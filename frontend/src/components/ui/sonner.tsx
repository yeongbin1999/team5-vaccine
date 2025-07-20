'use client';

import { useTheme } from 'next-themes';
import { Toaster as Sonner, ToasterProps, toast as sonnerToast } from 'sonner';
import { cn } from '@/lib/utils';

const Toaster = ({ className, ...props }: ToasterProps) => {
  const { theme = 'system' } = useTheme();

  return (
    <Sonner
      theme={theme as ToasterProps['theme']}
      className={cn('toaster group', className)}
      style={
        {
          '--normal-bg': 'var(--popover)',
          '--normal-text': 'var(--popover-foreground)',
          '--normal-border': 'var(--border)',
        } as React.CSSProperties
      }
      {...props}
    />
  );
};

export { Toaster, sonnerToast as toast };
