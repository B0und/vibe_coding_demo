import React from "react";
import { classNames } from "../../utils/helpers";

export interface Column<T = any> {
  key: string;
  title: string;
  render?: (value: any, record: T, index: number) => React.ReactNode;
  width?: string;
  align?: "left" | "center" | "right";
  sortable?: boolean;
}

export interface TableProps<T = any> {
  data: T[];
  columns: Column<T>[];
  loading?: boolean;
  emptyText?: string;
  onRowClick?: (record: T, index: number) => void;
  className?: string;
  size?: "sm" | "md" | "lg";
  striped?: boolean;
  bordered?: boolean;
}

function Table<T = any>({
  data,
  columns,
  loading = false,
  emptyText = "No data available",
  onRowClick,
  className,
  size = "md",
  striped = false,
  bordered = true,
}: TableProps<T>) {
  const sizeClasses = {
    sm: "text-xs",
    md: "text-sm",
    lg: "text-base",
  };

  const cellPaddingClasses = {
    sm: "px-2 py-1",
    md: "px-4 py-2",
    lg: "px-6 py-3",
  };

  const getValue = (record: T, key: string): any => {
    return key.split(".").reduce((obj, k) => obj?.[k], record as any);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className={classNames("overflow-x-auto", className)}>
      <table
        className={classNames(
          "min-w-full divide-y divide-secondary-200",
          sizeClasses[size]
        )}
      >
        {/* Header */}
        <thead className="bg-secondary-50">
          <tr>
            {columns.map((column) => (
              <th
                key={column.key}
                className={classNames(
                  "font-medium text-secondary-900 tracking-wider",
                  cellPaddingClasses[size],
                  {
                    "text-left": column.align === "left" || !column.align,
                    "text-center": column.align === "center",
                    "text-right": column.align === "right",
                  }
                )}
                style={{ width: column.width }}
              >
                {column.title}
              </th>
            ))}
          </tr>
        </thead>

        {/* Body */}
        <tbody
          className={classNames("bg-white divide-y divide-secondary-200", {
            "divide-y-0": !bordered,
          })}
        >
          {data.length === 0 ? (
            <tr>
              <td
                colSpan={columns.length}
                className={classNames(
                  "text-center text-secondary-500",
                  cellPaddingClasses[size]
                )}
              >
                {emptyText}
              </td>
            </tr>
          ) : (
            data.map((record, index) => (
              <tr
                key={index}
                className={classNames({
                  "hover:bg-secondary-50 cursor-pointer": !!onRowClick,
                  "bg-secondary-25": striped && index % 2 === 1,
                })}
                onClick={() => onRowClick?.(record, index)}
              >
                {columns.map((column) => {
                  const value = getValue(record, column.key);
                  const content = column.render
                    ? column.render(value, record, index)
                    : value;

                  return (
                    <td
                      key={column.key}
                      className={classNames(
                        "text-secondary-900 whitespace-nowrap",
                        cellPaddingClasses[size],
                        {
                          "text-left": column.align === "left" || !column.align,
                          "text-center": column.align === "center",
                          "text-right": column.align === "right",
                          "border-r border-secondary-200": bordered,
                        }
                      )}
                    >
                      {content}
                    </td>
                  );
                })}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

Table.displayName = "Table";

export default Table;
