# Software Requirements Specification (SRS)
## Aplikasi Laundry Desktop

Version: 1.0  
Date: 2026  
Author: Development Team

---

# 1. Pendahuluan

## 1.1 Tujuan
Dokumen ini menjelaskan kebutuhan sistem (Software Requirements Specification) untuk aplikasi **Laundry Desktop** yang digunakan untuk mengelola operasional bisnis laundry seperti transaksi, pelanggan, layanan, pembayaran, dan laporan.

Dokumen ini ditujukan untuk:
- Developer
- System analyst
- Pemilik usaha laundry

---

## 1.2 Ruang Lingkup Sistem

Aplikasi Laundry Desktop adalah sistem yang digunakan untuk:

- Mengelola pelanggan
- Mengelola layanan laundry
- Mengelola transaksi laundry
- Menghitung harga berdasarkan kombinasi layanan
- Mengelola pembayaran
- Melacak status cucian
- Menghasilkan laporan operasional

Sistem ini dirancang sebagai **aplikasi desktop offline-first** yang menggunakan database lokal.

---

## 1.3 Definisi dan Istilah

| Istilah | Definisi |
|------|------|
| Order | Transaksi laundry dari pelanggan |
| Service | Jenis layanan laundry |
| Speed | Kecepatan penyelesaian |
| Unit | Satuan layanan |
| Invoice | Nomor nota transaksi |
| Qty | Jumlah satuan layanan |

---

# 2. Deskripsi Umum Sistem

## 2.1 Perspektif Produk

Aplikasi ini merupakan sistem **standalone desktop application** yang berjalan pada komputer kasir laundry.

Komponen utama sistem:

- UI Desktop
- Business Logic
- Database Lokal
- Reporting Module

---

## 2.2 Fungsi Utama Sistem

Sistem harus menyediakan fitur berikut:

1. Manajemen pelanggan
2. Manajemen layanan
3. Manajemen harga layanan
4. Transaksi laundry
5. Tracking status laundry
6. Pembayaran
7. Cetak nota
8. Laporan operasional
9. Manajemen pengguna

---

## 2.3 Karakteristik Pengguna

| Jenis User | Deskripsi |
|------|------|
| Admin | Mengelola konfigurasi sistem |
| Kasir | Menginput transaksi laundry |
| Owner | Melihat laporan dan statistik |

---

# 3. Fitur Sistem

# 3.1 Manajemen Pelanggan

Sistem harus memungkinkan pengguna untuk:

- Menambahkan pelanggan
- Mengedit data pelanggan
- Menghapus pelanggan
- Melihat riwayat transaksi pelanggan

### Data pelanggan

- ID pelanggan
- Nama
- Nomor telepon
- Alamat
- Catatan

---

# 3.2 Manajemen Layanan Laundry

Sistem harus memungkinkan admin untuk:

- Menambah jenis layanan
- Mengubah layanan
- Menghapus layanan

Contoh layanan:

- Cuci
- Setrika
- Cuci + Setrika
- Dry Clean

---

# 3.3 Manajemen Kecepatan Layanan

Admin dapat menentukan tingkat kecepatan layanan:

Contoh:

| Speed | Durasi |
|------|------|
| Normal | 48 jam |
| Express | 24 jam |
| Super Express | 6 jam |

---

# 3.4 Manajemen Satuan

Sistem mendukung beberapa satuan layanan:

| Satuan | Contoh |
|------|------|
| Kg | Laundry kiloan |
| Pcs | Selimut |
| Item | Sepatu |
| Meter | Karpet |

---

# 3.5 Sistem Penentuan Harga

Harga layanan ditentukan berdasarkan kombinasi:

Contoh:

| Service | Speed | Unit | Price |
|------|------|------|------|
| Cuci + Setrika | Normal | Kg | 7000 |
| Cuci + Setrika | Express | Kg | 10000 |
| Dry Clean | Normal | Pcs | 15000 |

---

# 3.6 Transaksi Laundry

Kasir dapat membuat transaksi laundry dengan langkah berikut:

1. Pilih pelanggan
2. Pilih layanan
3. Pilih kecepatan
4. Pilih satuan
5. Input quantity
6. Sistem menghitung harga
7. Simpan transaksi

Data transaksi:

- ID order
- Tanggal masuk
- Estimasi selesai
- Status
- Total harga

---

# 3.7 Status Laundry

Order laundry memiliki status berikut:

| Status |
|------|
| Diterima |
| Dicuci |
| Disetrika |
| Selesai |
| Sudah Diambil |

---

# 3.8 Pembayaran

Sistem mendukung:

- Pembayaran tunai
- Pembayaran digital
- Pembayaran DP

Status pembayaran:

- Belum Lunas
- Lunas

---

# 3.9 Cetak Nota

Sistem harus dapat mencetak nota yang berisi:

- Nomor invoice
- Nama pelanggan
- Detail layanan
- Total harga
- Status pembayaran
- Tanggal transaksi

---

# 3.10 Laporan

Sistem harus menyediakan laporan berikut:

### Laporan Transaksi
- Harian
- Mingguan
- Bulanan

### Laporan Pendapatan
- Total omzet
- Layanan paling laku

### Laporan Pelanggan
- Pelanggan paling aktif

---

# 3.11 Dashboard

Dashboard menampilkan:

- Order hari ini
- Order belum selesai
- Pendapatan hari ini
- Pendapatan bulan ini

---

# 4. Kebutuhan Non-Fungsional

## 4.1 Performance
Sistem harus mampu memproses transaksi dalam waktu < 2 detik.

---

## 4.2 Reliability
Database harus dapat di-backup secara manual.

---

## 4.3 Security
Sistem harus memiliki:

- Login pengguna
- Hak akses berdasarkan role

---

## 4.4 Usability
Sistem harus mudah digunakan oleh kasir dengan pelatihan minimal.

---

## 4.5 Portability
Aplikasi harus dapat berjalan pada sistem operasi:

- Windows

---

# 5. Teknologi yang Digunakan

| Komponen | Teknologi |
|------|------|
| Bahasa | Java |
| GUI | JavaFX |
| Database | SQLite |
| IDE | Visual Studio Code |

---

# 6. Model Data (High Level)

Entitas utama sistem:

- Users
- Customers
- Services
- Speeds
- Units
- ServicePrices
- Orders
- OrderDetails
- Payments

---

# 7. Batasan Sistem

- Sistem hanya berjalan secara lokal (offline)
- Tidak mendukung multi-cabang pada versi awal
- Integrasi payment gateway tidak tersedia pada versi awal

---

# 8. Pengembangan Masa Depan

Fitur yang dapat ditambahkan di masa depan:

- Barcode pada nota
- Notifikasi WhatsApp ketika laundry selesai
- Sistem member / deposit
- Multi cabang
- Aplikasi mobile pelanggan