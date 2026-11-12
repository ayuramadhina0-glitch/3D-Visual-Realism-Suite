# 3D Visual Realism Suite
Project praktikum Grafika Komputer Minggu 11. Maven, Java 17, LWJGL 3.3.3, JOML 1.10.5.

## Menjalankan
1. Buka folder project ini melalui NetBeans (Open Project) atau IntelliJ IDEA.
2. Pastikan JDK 17 tersedia dan koneksi internet aktif untuk mengunduh dependency Maven.
3. Jalankan `mvn clean compile exec:java`.
4. Di NetBeans, jalankan main class `com.grafkom.viewing3d.ThreeDViewing`.

## Kontrol
- B: Phong / Blinn-Phong
- F: Flat / Smooth
- Z: Z-Buffer ON/OFF
- C: Back-Face Culling ON/OFF
- N: Nearest / Bilinear Mipmapped
- S: Shadow Mapping ON/OFF
- ESC: Keluar

Catatan: project ini merupakan implementasi mandiri yang memakai koordinat Maven/package ThreeDViewing milik project sebelumnya. Jika project lama memiliki fitur kamera atau kelas tambahan, gabungkan/backup dahulu sebelum mengganti folder.
