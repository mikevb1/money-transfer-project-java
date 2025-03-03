export const translateStatus = (status: string): string => {
  const translations: { [key: string]: string } = {
    PENDING: "In Afwachting",
    APPROVED: "Goedgekeurd",
    DECLINED: "Afgekeurd",
    IN_PROGRESS: "In Behandeling"
  };
  
  return translations[status] || status;
}; 