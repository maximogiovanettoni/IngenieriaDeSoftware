import React, { useState, useEffect } from 'react';
import { ShoppingCart } from 'lucide-react';
import { Link } from 'wouter';
import styles from './CartBadge.module.css';

interface CartBadgeProps {
  itemCount: number;
}

export const CartBadge: React.FC<CartBadgeProps> = ({ itemCount }) => {
  const [isAnimating, setIsAnimating] = useState(false);
  const [prevCount, setPrevCount] = useState(itemCount);

  useEffect(() => {
    if (itemCount > prevCount) {
      setIsAnimating(true);
      const timer = setTimeout(() => {
        setIsAnimating(false);
      }, 400);
      return () => clearTimeout(timer);
    }
    setPrevCount(itemCount);
  }, [itemCount, prevCount]);

  return (
    <Link href="/menu" className={styles.cartIcon}>
      <ShoppingCart size={20} />
      {itemCount > 0 && (
        <span className={`${styles.badge} ${isAnimating ? styles.animate : ''}`}>
          {itemCount > 99 ? '99+' : itemCount}
        </span>
      )}
    </Link>
  );
};
