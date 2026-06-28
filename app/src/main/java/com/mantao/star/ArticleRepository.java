package com.mantao.star;

import java.util.ArrayList;
import java.util.List;

/**
 * Sumber data artikel statis. Konten ditulis sendiri sebagai materi edukasi umum
 * seputar lingkungan & pengelolaan sampah — bukan kutipan dari sumber lain.
 */
public class ArticleRepository {

    public static List<Article> getAll() {
        List<Article> list = new ArrayList<>();

        list.add(new Article(
                "zero_waste",
                "Tips & Trik",
                "🌍",
                "Mengenal Zero Waste: Masa Depan yang Bersih",
                "Langkah awal menuju gaya hidup berkelanjutan yang mampu mengurangi jejak sampahmu secara signifikan.",
                "Zero waste adalah gaya hidup yang bertujuan meminimalkan sampah yang berakhir di tempat " +
                        "pembuangan akhir (TPA) atau dibakar di insinerator. Filosofi ini berangkat dari kesadaran " +
                        "bahwa sebagian besar barang yang kita buang sebenarnya masih bisa dikurangi, digunakan " +
                        "ulang, atau didaur ulang.\n\n" +
                        "Konsep ini sering dijelaskan lewat prinsip 5R: Refuse (menolak barang sekali pakai yang " +
                        "tidak perlu), Reduce (mengurangi konsumsi), Reuse (menggunakan ulang barang yang masih " +
                        "layak), Repurpose (memanfaatkan kembali barang untuk fungsi baru), dan Recycle " +
                        "(mendaur ulang sebagai pilihan terakhir).\n\n" +
                        "Memulai zero waste tidak perlu langsung sempurna. Langkah kecil seperti membawa tas " +
                        "belanja sendiri, menolak sedotan plastik, atau memilah sampah organik dan anorganik di " +
                        "rumah sudah merupakan kontribusi nyata. Yang penting adalah konsistensi, bukan " +
                        "kesempurnaan.\n\n" +
                        "Dengan semakin banyak orang menerapkan gaya hidup ini, volume sampah yang masuk ke TPA " +
                        "bisa berkurang secara bertahap, sekaligus menumbuhkan kebiasaan konsumsi yang lebih " +
                        "bertanggung jawab terhadap lingkungan.",
                6,
                true
        ));

        list.add(new Article(
                "tips_plastik",
                "Tips & Trik",
                "♻️",
                "5 Tips Mengurangi Sampah Plastik",
                "Langkah sederhana yang bisa langsung kamu terapkan hari ini.",
                "Sampah plastik butuh waktu sangat lama untuk terurai secara alami, dan sebagian besar di " +
                        "antaranya berakhir di lingkungan. Berikut lima langkah sederhana yang bisa langsung kamu " +
                        "terapkan untuk mengurangi penggunaan plastik sehari-hari:\n\n" +
                        "1. Bawa tas belanja sendiri setiap kali pergi ke pasar atau supermarket, sehingga tidak " +
                        "perlu menerima kantong plastik baru.\n\n" +
                        "2. Gunakan botol minum dan kotak makan yang bisa dipakai ulang, daripada membeli air " +
                        "kemasan atau makanan dengan kemasan sekali pakai.\n\n" +
                        "3. Tolak sedotan plastik kalau memang tidak diperlukan, atau bawa sedotan stainless/bambu " +
                        "sendiri.\n\n" +
                        "4. Beli kebutuhan dalam kemasan besar (refill) untuk mengurangi jumlah kemasan kecil yang " +
                        "dipakai berulang.\n\n" +
                        "5. Pilih produk dengan kemasan minim plastik, atau cari toko curah yang memungkinkan kamu " +
                        "membawa wadah sendiri.\n\n" +
                        "Kelima kebiasaan ini sederhana, tapi kalau dilakukan secara konsisten oleh banyak orang, " +
                        "dampaknya terhadap pengurangan sampah plastik bisa sangat besar.",
                5,
                false
        ));

        list.add(new Article(
                "kondisi_tpa",
                "Berita Lingkungan",
                "🏭",
                "Kondisi TPA Terkini: Menuju Kapasitas Maksimum",
                "Investigasi STAR mengenai lonjakan volume sampah di wilayah urban dalam beberapa waktu terakhir.",
                "Banyak tempat pembuangan akhir (TPA) di kota-kota besar menghadapi tekanan kapasitas yang " +
                        "semakin berat, seiring pertumbuhan jumlah penduduk dan volume konsumsi yang terus " +
                        "meningkat. Ketika TPA mendekati atau melewati kapasitas rancangannya, risiko pencemaran " +
                        "air tanah, gas metana, hingga bencana seperti longsor sampah ikut meningkat.\n\n" +
                        "Salah satu akar masalahnya adalah rendahnya tingkat pemilahan sampah dari sumbernya. " +
                        "Sampah organik dan anorganik yang tercampur membuat proses pengolahan di TPA menjadi " +
                        "lebih sulit dan memperpendek usia pakai lahan TPA itu sendiri.\n\n" +
                        "Beberapa kota mulai menerapkan strategi pengurangan sampah dari hulu, seperti program " +
                        "bank sampah, insentif daur ulang, hingga edukasi pemilahan di tingkat rumah tangga — " +
                        "termasuk lewat aplikasi seperti STAR yang membantu warga memilah dan melaporkan sampah " +
                        "lebih awal.\n\n" +
                        "Mengurangi beban TPA bukan cuma tugas pemerintah kota, tapi juga kebiasaan kecil yang " +
                        "dimulai dari rumah masing-masing: memilah sampah, mengurangi konsumsi barang sekali " +
                        "pakai, dan mendukung program daur ulang di lingkungan sekitar.",
                7,
                false
        ));

        list.add(new Article(
                "teknologi_organik",
                "Inovasi Hijau",
                "🌱",
                "Teknologi Pengolah Sampah Organik Rumahan",
                "Beberapa metode pengomposan rumahan yang bisa kamu coba mulai dari sekarang.",
                "Sampah organik rumah tangga — seperti sisa sayur, kulit buah, dan sisa makanan — sebenarnya " +
                        "bisa diolah langsung di rumah tanpa perlu dibuang ke TPA. Beberapa metode pengomposan " +
                        "rumahan yang populer antara lain:\n\n" +
                        "Komposter takakura, yaitu metode pengomposan sederhana menggunakan keranjang berlapis " +
                        "sekam dan kain, cocok untuk rumah dengan lahan terbatas.\n\n" +
                        "Metode bokashi, yang menggunakan campuran mikroorganisme fermentasi untuk mempercepat " +
                        "penguraian sampah organik, termasuk sisa makanan yang sudah dimasak atau berminyak.\n\n" +
                        "Komposter elektrik, alat modern yang bisa mengubah sisa makanan menjadi kompos kering " +
                        "dalam beberapa jam, meski harganya relatif lebih mahal dibanding metode manual.\n\n" +
                        "Hasil kompos dari metode-metode ini bisa dipakai langsung untuk menyuburkan tanaman di " +
                        "rumah, sehingga sampah organik tidak hanya berkurang volumenya, tapi juga punya manfaat " +
                        "tambahan. Memulai komposting rumahan adalah salah satu cara paling efektif untuk " +
                        "mengurangi sampah organik yang biasanya menyumbang porsi besar dari total sampah rumah " +
                        "tangga.",
                8,
                false
        ));

        list.add(new Article(
                "komunitas_hero",
                "Komunitas",
                "👥",
                "Hero STAR Bulan Ini: Komunitas Plastik Hijau",
                "Mengenal kontribusi nyata komunitas-komunitas yang aktif di isu lingkungan sekitar.",
                "Setiap bulan, tim STAR ingin menyoroti komunitas atau individu yang aktif bergerak di isu " +
                        "lingkungan dan pengelolaan sampah di lingkungan sekitar mereka. Pengakuan ini bukan soal " +
                        "kompetisi, tapi cara untuk merayakan kontribusi nyata yang sering tidak terlihat.\n\n" +
                        "Komunitas seperti ini biasanya bergerak dari hal sederhana: mengadakan kegiatan " +
                        "bersih-bersih lingkungan secara rutin, mengedukasi tetangga soal pemilahan sampah, atau " +
                        "membangun bank sampah skala RT/RW yang dikelola bersama warga.\n\n" +
                        "Kalau kamu punya komunitas atau tahu individu yang aktif melakukan aksi nyata seputar " +
                        "pengelolaan sampah dan lingkungan di sekitarmu, jangan ragu untuk membagikan ceritanya — " +
                        "siapa tahu jadi inspirasi untuk pengguna STAR lainnya di bulan-bulan berikutnya.\n\n" +
                        "(Catatan: fitur pengajuan cerita komunitas langsung dari aplikasi masih dalam " +
                        "pengembangan dan belum tersedia di versi ini.)",
                4,
                false
        ));

        list.add(new Article(
                "regulasi_eropa",
                "Berita Lingkungan",
                "🇪🇺",
                "Regulasi Baru Pengelolaan Limbah di Uni Eropa",
                "Tren regulasi global yang juga relevan untuk arah kebijakan sampah di Indonesia.",
                "Uni Eropa telah lama menjadi salah satu kawasan paling aktif dalam mendorong regulasi " +
                        "pengelolaan limbah yang lebih ketat. Salah satu kebijakan yang paling dikenal adalah " +
                        "Single-Use Plastics Directive, yang membatasi atau melarang penggunaan sejumlah produk " +
                        "plastik sekali pakai seperti sedotan, alat makan plastik, dan cotton bud bertangkai " +
                        "plastik di negara-negara anggotanya.\n\n" +
                        "Selain itu, Uni Eropa juga mendorong prinsip extended producer responsibility (EPR), di " +
                        "mana produsen ikut bertanggung jawab atas pengelolaan kemasan produk mereka setelah " +
                        "dipakai konsumen — termasuk biaya daur ulang dan pengumpulannya.\n\n" +
                        "Kebijakan semacam ini menjadi salah satu acuan bagi negara lain, termasuk Indonesia, " +
                        "dalam merancang regulasi pengelolaan sampah dan kemasan plastik ke depannya. Meski " +
                        "penerapannya berbeda-beda di tiap negara, arah kebijakan global semakin mengarah ke " +
                        "pengurangan plastik sekali pakai dan tanggung jawab produsen yang lebih besar.\n\n" +
                        "Bagi individu, perkembangan regulasi semacam ini juga jadi pengingat bahwa perubahan " +
                        "kebijakan besar biasanya berjalan seiring dengan perubahan kebiasaan kecil dari " +
                        "masyarakat — termasuk usaha memilah dan mengurangi sampah sehari-hari.",
                10,
                false
        ));

        return list;
    }

    public static Article getById(String id) {
        if (id == null) return null;
        for (Article a : getAll()) {
            if (a.id.equals(id)) return a;
        }
        return null;
    }
}