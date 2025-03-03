import toast, { Toast } from 'react-hot-toast';
import React from 'react';

class ToastManager {
  private static activeToasts: Toast[] = [];
  private static readonly MAX_TOASTS = 3;

  static show(message: string, type: 'success' | 'error') {
    // Verwijder de oudste toast als we aan het maximum zitten
    if (this.activeToasts.length >= this.MAX_TOASTS) {
      const oldestToast = this.activeToasts[0];
      toast.dismiss(oldestToast.id);
      this.activeToasts.shift();
    }

    // Maak nieuwe toast aan met klikbare optie
    const newToast = toast[type](message, {
      duration: 5000,
      id: Date.now().toString(),
      style: {
        cursor: 'pointer',
      },
      onClick: () => toast.dismiss(newToast.id),
    });

    // Voeg toe aan actieve toasts
    this.activeToasts.push(newToast);

    // Verwijder uit de lijst wanneer de toast verdwijnt
    setTimeout(() => {
      this.activeToasts = this.activeToasts.filter(t => t.id !== newToast.id);
    }, 5000);
  }

  static success(message: string) {
    this.show(message, 'success');
  }

  static error(message: string) {
    this.show(message, 'error');
  }
}

export default ToastManager; 