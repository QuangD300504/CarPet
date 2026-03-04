import { useState, useMemo } from 'react';

export function usePagination<T>(items: T[], itemsPerPage: number = 8) {
    const [currentPage, setCurrentPage] = useState(1);
    
    const paginatedItems = useMemo(() => {
        const start = (currentPage - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        return items.slice(start, end);
    }, [items, currentPage, itemsPerPage]);

    const totalPages = Math.ceil(items.length / itemsPerPage);

    const handlePageChange = (page: number) => {
        if (page < 1 || page > totalPages) return;
        setCurrentPage(page);
    };

    // Reset current page when items change or it exceeds total pages
    if (currentPage > totalPages && totalPages > 0) {
        setCurrentPage(totalPages);
    }

    return {
        currentPage,
        totalPages,
        paginatedItems,
        handlePageChange,
        totalItems: items.length,
        itemsPerPage
    };
}
