import React, { useState } from "react";
import Input from "./Input";
import type { InputProps } from "./Input";

export interface SearchInputProps
  extends Omit<InputProps, "leftIcon" | "rightIcon" | "type"> {
  onSearch?: (value: string) => void;
  onClear?: () => void;
  showClearButton?: boolean;
  searchOnType?: boolean;
  debounceMs?: number;
}

const SearchInput = React.forwardRef<HTMLInputElement, SearchInputProps>(
  (
    {
      onSearch,
      onClear,
      showClearButton = true,
      searchOnType = true,
      debounceMs = 300,
      value,
      onChange,
      placeholder = "Search...",
      ...props
    },
    ref
  ) => {
    const [internalValue, setInternalValue] = useState(value || "");
    const [debounceTimer, setDebounceTimer] = useState<NodeJS.Timeout | null>(
      null
    );

    const currentValue = value !== undefined ? value : internalValue;
    const hasValue = currentValue && currentValue.toString().length > 0;

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const newValue = e.target.value;

      if (value === undefined) {
        setInternalValue(newValue);
      }

      onChange?.(e);

      if (searchOnType && onSearch) {
        // Clear existing timer
        if (debounceTimer) {
          clearTimeout(debounceTimer);
        }

        // Set new timer
        const timer = setTimeout(() => {
          onSearch(newValue);
        }, debounceMs);

        setDebounceTimer(timer);
      }
    };

    const handleClear = () => {
      const clearedValue = "";

      if (value === undefined) {
        setInternalValue(clearedValue);
      }

      // Create synthetic event for onChange
      const syntheticEvent = {
        target: { value: clearedValue },
        currentTarget: { value: clearedValue },
      } as React.ChangeEvent<HTMLInputElement>;

      onChange?.(syntheticEvent);
      onClear?.();
      onSearch?.(clearedValue);

      // Clear debounce timer
      if (debounceTimer) {
        clearTimeout(debounceTimer);
        setDebounceTimer(null);
      }
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "Enter" && onSearch) {
        e.preventDefault();
        onSearch(currentValue.toString());
      }
    };

    const searchIcon = (
      <svg
        className="w-5 h-5"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
        />
      </svg>
    );

    const clearButton = hasValue && showClearButton && (
      <button
        type="button"
        onClick={handleClear}
        className="text-secondary-400 hover:text-secondary-600 focus:outline-none focus:text-secondary-600 transition-colors"
        aria-label="Clear search"
      >
        <svg
          className="w-5 h-5"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M6 18L18 6M6 6l12 12"
          />
        </svg>
      </button>
    );

    return (
      <Input
        ref={ref}
        type="search"
        value={currentValue}
        onChange={handleChange}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        leftIcon={searchIcon}
        rightIcon={clearButton}
        {...props}
      />
    );
  }
);

SearchInput.displayName = "SearchInput";

export default SearchInput;
