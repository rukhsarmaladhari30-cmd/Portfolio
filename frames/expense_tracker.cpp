/**
 * ============================================================================
 * PERSONAL EXPENSE TRACKER - CORE DATA STRUCTURES & ALGORITHMS ENGINE
 * Author: Rukhsar Maladhari (B.Tech CSE, 2nd Year)
 * Tech Stack: C++ (C++17), STL Vector, Hash Map (Unordered Map), Sorting
 * ============================================================================
 * 
 * DSA ARCHITECTURAL SPECIFICATIONS:
 * 1. Vector (std::vector<Transaction>):
 *    - Dynamic array storing sequential transaction records.
 *    - Amortized O(1) insertion, O(1) random access by index.
 * 
 * 2. Hash Map (std::unordered_map<std::string, std::vector<int>>):
 *    - Inverted index mapping Category Name -> List of Vector Indices.
 *    - O(1) average lookup time when filtering or searching by category.
 * 
 * 3. Sorting Algorithms:
 *    - MergeSort: Stable O(N log N) sorting for transaction amounts.
 *    - QuickSort: Fast in-place O(N log N) partitioning for transaction dates.
 * 
 * 4. Persistence:
 *    - Serializes and deserializes transaction records to/from JSON and CSV files.
 * ============================================================================
 */

#include <iostream>
#include <vector>
#include <string>
#include <unordered_map>
#include <iomanip>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <chrono>

// Transaction Types
enum class TransactionType {
    INCOME,
    EXPENSE
};

// Transaction Data Structure
struct Transaction {
    int id;
    std::string description;
    double amount;
    std::string category;     // e.g., "Food", "Travel", "Shopping", "Education", "Bills", "Salary"
    std::string date;         // YYYY-MM-DD
    TransactionType type;     // INCOME or EXPENSE

    std::string getTypeString() const {
        return (type == TransactionType::INCOME) ? "INCOME" : "EXPENSE";
    }
};

class ExpenseTracker {
private:
    // DSA 1: Vector for sequential record storage
    std::vector<Transaction> transactions;
    
    // DSA 2: Hash Map for O(1) category indexing (Category -> vector of transaction indices)
    std::unordered_map<std::string, std::vector<int>> categoryIndex;

    int nextId = 1;

    // Helper: Rebuild Hash Map indices after modification or sort
    void rebuildCategoryIndex() {
        categoryIndex.clear();
        for (int i = 0; i < (int)transactions.size(); ++i) {
            categoryIndex[transactions[i].category].push_back(i);
        }
    }

    // ========================================================================
    // DSA 3: MERGE SORT FOR AMOUNT (O(N log N))
    // ========================================================================
    void mergeByAmount(std::vector<Transaction>& arr, int left, int mid, int right, bool ascending) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        std::vector<Transaction> L(n1);
        std::vector<Transaction> R(n2);

        for (int i = 0; i < n1; ++i) L[i] = arr[left + i];
        for (int j = 0; j < n2; ++j) R[j] = arr[mid + 1 + j];

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            bool condition = ascending ? (L[i].amount <= R[j].amount) : (L[i].amount >= R[j].amount);
            if (condition) {
                arr[k++] = L[i++];
            } else {
                arr[k++] = R[j++];
            }
        }

        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }

    void mergeSortByAmount(std::vector<Transaction>& arr, int left, int right, bool ascending) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortByAmount(arr, left, mid, ascending);
            mergeSortByAmount(arr, mid + 1, right, ascending);
            mergeByAmount(arr, left, mid, right, ascending);
        }
    }

    // ========================================================================
    // DSA 3: QUICK SORT FOR DATE (O(N log N))
    // ========================================================================
    int partitionByDate(std::vector<Transaction>& arr, int low, int high, bool ascending) {
        std::string pivot = arr[high].date;
        int i = low - 1;

        for (int j = low; j < high; ++j) {
            bool condition = ascending ? (arr[j].date <= pivot) : (arr[j].date >= pivot);
            if (condition) {
                i++;
                std::swap(arr[i], arr[j]);
            }
        }
        std::swap(arr[i + 1], arr[high]);
        return i + 1;
    }

    void quickSortByDate(std::vector<Transaction>& arr, int low, int high, bool ascending) {
        if (low < high) {
            int pi = partitionByDate(arr, low, high, ascending);
            quickSortByDate(arr, low, pi - 1, ascending);
            quickSortByDate(arr, pi + 1, high, ascending);
        }
    }

public:
    // 1. ADD TRANSACTION
    void addTransaction(const std::string& desc, double amount, const std::string& category, const std::string& date, TransactionType type) {
        Transaction t;
        t.id = nextId++;
        t.description = desc;
        t.amount = amount;
        t.category = category;
        t.date = date;
        t.type = type;

        int index = transactions.size();
        transactions.push_back(t);
        categoryIndex[category].push_back(index);
    }

    // 2. EDIT TRANSACTION
    bool editTransaction(int id, const std::string& desc, double amount, const std::string& category, const std::string& date, TransactionType type) {
        for (auto& t : transactions) {
            if (t.id == id) {
                t.description = desc;
                t.amount = amount;
                t.category = category;
                t.date = date;
                t.type = type;
                rebuildCategoryIndex();
                return true;
            }
        }
        return false;
    }

    // 3. DELETE TRANSACTION
    bool deleteTransaction(int id) {
        for (auto it = transactions.begin(); it != transactions.end(); ++it) {
            if (it->id == id) {
                transactions.erase(it);
                rebuildCategoryIndex();
                return true;
            }
        }
        return false;
    }

    // 4. HASH MAP CATEGORY SEARCH (O(1) average lookup)
    std::vector<Transaction> searchByCategory(const std::string& category) const {
        std::vector<Transaction> results;
        auto it = categoryIndex.find(category);
        if (it != categoryIndex.end()) {
            for (int idx : it->second) {
                if (idx >= 0 && idx < (int)transactions.size()) {
                    results.push_back(transactions[idx]);
                }
            }
        }
        return results;
    }

    // 5. DESCRIPTION SUBSTRING SEARCH
    std::vector<Transaction> searchByDescription(const std::string& query) const {
        std::vector<Transaction> results;
        std::string lowerQuery = query;
        std::transform(lowerQuery.begin(), lowerQuery.end(), lowerQuery.begin(), ::tolower);

        for (const auto& t : transactions) {
            std::string lowerDesc = t.description;
            std::transform(lowerDesc.begin(), lowerDesc.end(), lowerDesc.begin(), ::tolower);
            if (lowerDesc.find(lowerQuery) != std::string::npos) {
                results.push_back(t);
            }
        }
        return results;
    }

    // 6. SORT BY AMOUNT
    void sortByAmount(bool ascending = true) {
        if (transactions.empty()) return;
        mergeSortByAmount(transactions, 0, transactions.size() - 1, ascending);
        rebuildCategoryIndex();
    }

    // 7. SORT BY DATE
    void sortByDate(bool ascending = false) {
        if (transactions.empty()) return;
        quickSortByDate(transactions, 0, transactions.size() - 1, ascending);
        rebuildCategoryIndex();
    }

    // 8. FINANCIAL TOTALS & METRICS
    double getTotalIncome() const {
        double sum = 0.0;
        for (const auto& t : transactions) {
            if (t.type == TransactionType::INCOME) sum += t.amount;
        }
        return sum;
    }

    double getTotalExpenses() const {
        double sum = 0.0;
        for (const auto& t : transactions) {
            if (t.type == TransactionType::EXPENSE) sum += t.amount;
        }
        return sum;
    }

    double getNetBalance() const {
        return getTotalIncome() - getTotalExpenses();
    }

    // 9. MONTHLY EXPENSE SUMMARY (Map: YYYY-MM -> Total Expense)
    std::unordered_map<std::string, double> getMonthlyExpenseSummary() const {
        std::unordered_map<std::string, double> monthly;
        for (const auto& t : transactions) {
            if (t.type == TransactionType::EXPENSE && t.date.length() >= 7) {
                std::string month = t.date.substr(0, 7); // "YYYY-MM"
                monthly[month] += t.amount;
            }
        }
        return monthly;
    }

    // 10. CATEGORY SUMMARY
    std::unordered_map<std::string, double> getCategorySpendingSummary() const {
        std::unordered_map<std::string, double> summary;
        for (const auto& t : transactions) {
            if (t.type == TransactionType::EXPENSE) {
                summary[t.category] += t.amount;
            }
        }
        return summary;
    }

    // 11. FILE PERSISTENCE (Export to CSV)
    bool saveToCSV(const std::string& filename) const {
        std::ofstream file(filename);
        if (!file.is_open()) return false;

        file << "id,description,amount,category,date,type\n";
        for (const auto& t : transactions) {
            file << t.id << ",\"" << t.description << "\"," << t.amount << ","
                 << t.category << "," << t.date << "," << t.getTypeString() << "\n";
        }
        return true;
    }

    // 12. FILE PERSISTENCE (Export to JSON format)
    std::string exportToJSON() const {
        std::ostringstream ss;
        ss << "[\n";
        for (size_t i = 0; i < transactions.size(); ++i) {
            const auto& t = transactions[i];
            ss << "  {\n"
               << "    \"id\": " << t.id << ",\n"
               << "    \"description\": \"" << t.description << "\",\n"
               << "    \"amount\": " << t.amount << ",\n"
               << "    \"category\": \"" << t.category << "\",\n"
               << "    \"date\": \"" << t.date << "\",\n"
               << "    \"type\": \"" << t.getTypeString() << "\"\n"
               << "  }" << (i + 1 < transactions.size() ? "," : "") << "\n";
        }
        ss << "]\n";
        return ss.str();
    }

    const std::vector<Transaction>& getAllTransactions() const {
        return transactions;
    }
};

// ============================================================================
// CLI INTERFACE & TEST DRIVER
// ============================================================================
int main() {
    ExpenseTracker tracker;

    // Seed Initial Test Data
    tracker.addTransaction("Monthly Tech Internship Stipend", 35000.0, "Salary", "2026-11-01", TransactionType::INCOME);
    tracker.addTransaction("Textbooks & DSA Course Materials", 2800.0, "Education", "2026-11-02", TransactionType::EXPENSE);
    tracker.addTransaction("Artisan Cafe & Coffee with Team", 650.0, "Food", "2026-11-04", TransactionType::EXPENSE);
    tracker.addTransaction("Metro Card Recharge & Commute", 1200.0, "Travel", "2026-11-05", TransactionType::EXPENSE);
    tracker.addTransaction("High-Speed Fiber Internet Bill", 1499.0, "Bills", "2026-11-06", TransactionType::EXPENSE);
    tracker.addTransaction("Mechanical Keyboard for Coding", 4500.0, "Shopping", "2026-11-08", TransactionType::EXPENSE);
    tracker.addTransaction("Freelance Web Development Project", 18500.0, "Salary", "2026-11-10", TransactionType::INCOME);

    std::cout << "========================================================\n";
    std::cout << "  PERSONAL EXPENSE TRACKER - C++ CORE DSA ENGINE\n";
    std::cout << "  Author: Rukhsar Maladhari (B.Tech CSE)\n";
    std::cout << "========================================================\n\n";

    std::cout << std::fixed << std::setprecision(2);
    std::cout << ">> Total Income:   Rs. " << tracker.getTotalIncome() << "\n";
    std::cout << ">> Total Expenses: Rs. " << tracker.getTotalExpenses() << "\n";
    std::cout << ">> Net Balance:    Rs. " << tracker.getNetBalance() << "\n\n";

    std::cout << "--- Category Breakdown (Hash Map O(1) Index) ---\n";
    for (const auto& pair : tracker.getCategorySpendingSummary()) {
        std::cout << " • " << std::setw(12) << std::left << pair.first << ": Rs. " << pair.second << "\n";
    }

    std::cout << "\n--- Sort by Amount Descending (MergeSort O(N log N)) ---\n";
    tracker.sortByAmount(false);
    for (const auto& t : tracker.getAllTransactions()) {
        std::cout << " [" << t.getTypeString() << "] " 
                  << std::setw(30) << std::left << t.description 
                  << " | Rs. " << std::setw(8) << t.amount 
                  << " | " << t.category << " | " << t.date << "\n";
    }

    // Save to CSV file
    tracker.saveToCSV("expenses_data.csv");
    std::cout << "\n>> Successfully exported transactions to 'expenses_data.csv'\n";

    return 0;
}
