import React from "react";
import { classNames } from "../../utils/helpers";

export interface CardProps {
  children?: React.ReactNode;
  header?: React.ReactNode;
  footer?: React.ReactNode;
  className?: string;
  padding?: "none" | "sm" | "md" | "lg";
  shadow?: "none" | "sm" | "md" | "lg" | "xl";
  border?: boolean;
}

const Card: React.FC<CardProps> = ({
  children,
  header,
  footer,
  className,
  padding = "md",
  shadow = "md",
  border = true,
}) => {
  const baseClasses = "bg-white rounded-lg overflow-hidden";

  const paddingClasses = {
    none: "",
    sm: "p-3",
    md: "p-4",
    lg: "p-6",
  };

  const shadowClasses = {
    none: "",
    sm: "shadow-sm",
    md: "shadow-md",
    lg: "shadow-lg",
    xl: "shadow-xl",
  };

  const borderClasses = border ? "border border-secondary-200" : "";

  const cardClasses = classNames(
    baseClasses,
    shadowClasses[shadow],
    borderClasses,
    className
  );

  const contentPadding = paddingClasses[padding];

  return (
    <div className={cardClasses}>
      {header && (
        <div
          className={classNames(
            "border-b border-secondary-200",
            padding === "none" ? "px-4 py-3" : contentPadding
          )}
        >
          {header}
        </div>
      )}

      {children && <div className={contentPadding}>{children}</div>}

      {footer && (
        <div
          className={classNames(
            "border-t border-secondary-200 bg-secondary-50",
            padding === "none" ? "px-4 py-3" : contentPadding
          )}
        >
          {footer}
        </div>
      )}
    </div>
  );
};

Card.displayName = "Card";

export default Card;
