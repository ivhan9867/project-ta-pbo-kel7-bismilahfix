
---

## [v0.2.7] — 2026-04-29

### Changed

**[UI] Comprehensive UI Pass — semua screen**

**Global:**
- CSS root font-size naik dari 13px → 14px
- `UIFactory.sectionTitle()`: 10px → 11px, warna lebih terang (#8899AA)
- `UIFactory.vitalBar()`: label font 10px → 11px, bar height 6px → 7px

**Combat Screen (CombatView.java):**
- Enemy card: nama 11px → 14px bold, padding lebih lega, border left 3px rarity-style
- Ally card: lebar 110px → 150px, nama 9px → 12px, tampilkan role badge untuk merc
- Skill slots: ukuran 72×44px → 110×52px, nama skill 8px → 11px, CD label 8px → 10px
  hover effect baru, border left highlight untuk slot ready
- Action buttons: lebar 110px → 130px, font 11px → 13px, padding lebih besar,
  glow effect saat hover
- Combat log: tinggi 110px → 130px, font entry 10px → 12px

**Character Create (CharacterCreateView.java):**
- Lore text dan bonus preview: 10px → 12px

**Dungeon Map (DungeonMapView.java):**
- Teks info dan label: 9-10px → 11-12px

**Hub View (HubView.java):**
- Label info: 8-10px → 10-12px

**Profile View (ProfileView.java):**
- Teks stat: 9-10px → 11-12px

**Inventory / Shop / Mercenary (ViewsBundle.java):**
- Teks item row dan badge: 9-10px → 11-12px

**Merc Chat Panel (MercChatPanel.java):**
- Dialog text lebih besar agar mudah dibaca di panel 300px
