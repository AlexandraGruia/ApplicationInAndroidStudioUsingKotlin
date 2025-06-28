Descriere:
Aceasta este o aplicație Android dezvoltata in Kotlin și integrată cu Firebase Firestore pentru stocarea datelor și Cloudinary pentru stocarea imaginilor. Aplicația oferă funcționalități de jurnal, management foto, desen digital, text-to-speech, comunitate și multe altele.

Cerințe preliminare:
- Android Studio (recomandat: Arctic Fox sau mai nou)
- JDK 11 sau superior
- Dispozitiv fizic Android sau emulator (API 26+)
- Conexiune la internet (pentru funcționalitățile Firebase)
- Cont Firebase (pentru configurare proprie, dacă dorești să folosești propriul proiect)
- Cont Cloudinary (pentru configurare proprie, dacă dorești să folosești propriul cont)

Pași pentru rulare:
- Clonarea proiectului: gh repo clone AlexandraGruia/ApplicationInAndroidStudioUsingKotlin
- Deschiderea proiectul în Android Studio:
	 - Proiectul conține deja fișierul google-services.json pentru conectarea la Firebase
	 - Pentru folosirea propriului proiect Firebase: se creează un proiect nou în Firebase Console, se adaugăo aplicație Android și descarcă fișierul google-services.json, se înlocuiește fișierul existent din app/google-services.json
- Configurarea Cloudinary: 
	 - Proiectul este deja configurat pentru Cloudinary cu un cont de test
	 - Pentru folosirea propriului cont Cloudinary: se creează un cont pe Cloudinary Console, se obțin cloud_name, api_key și api_secret din Dashboard, se adaugă aceste valori în fișierul local.properties sau într-un fișier .env securizat: CLOUDINARY_CLOUD_NAME=your_cloud_name  
	   CLOUDINARY_API_KEY=your_api_key  
	   CLOUDINARY_API_SECRET=your_api_secret
- Sincronizarea și instalarea dependențelelor: 
	- Android Studio va descărca automat toate dependențele necesare (Gradle, Firebase, etc)
	- Dacă este necesar, se folosește opțiunea Sync Project with Gradle Files
- Rularea aplicației:
	- se selectează un emulator sau se conectează un dispozitiv fizic
	- se apasă pe butonul Run (sau Shift + F10)
  	- aplicația va fi instalată și lansată pe dispozitivul ales

Structura proiectului:
app/src/main/java/.../ui/ – Componente UI, activități, fragmente
app/src/main/java/.../database/ – Acces la date și servicii Firebase
app/src/main/java/.../models/ – Modele de date (entități)
app/google-services.json – Configurare Firebase
app/src/main/java/.../album/ – Gestionarea albumelor foto; include logica de încărcare a imaginilor în Cloudinary

