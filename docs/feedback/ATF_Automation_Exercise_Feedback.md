# Automation Exercise — cerințe business rămase

Consumerul verifică magazinul public prin journey-uri reale și reutilizează
browserul, adnotările, cleanup-ul și raportarea ATF. Contractul stabil și
comenzile sunt în [README-ul AutoEx](../../atf-test-auto-ex/README.md).

## ToDo — intenții business distincte

| ID | Cerință | Acceptanță | Prioritate |
|---|---|---|---:|
| `AUTOEX-CLEANUP-RECOVERY-01` | Confirmă recuperarea cleanup-ului după o întrerupere sub vignette | Proba live negativă trebuie să păstreze cauza inițială și captura, să navigheze la magazin și să confirme ștergerea identității acelei rulări. Un succes fără reclamă nu certifică acest traseu. | 76 |
| `AUTOEX-CART-EDIT-01` | Modificarea și eliminarea produselor din coș | Un journey distinct verifică recalcularea cantității/totalului și eliminarea rândului; nu repetă checkout-ul numai pentru alt profil de captură. | 67 |
| `AUTOEX-SESSION-01` | Continuitatea coșului după autentificare | Identitatea sintetică și produsele alese sunt păstrate după login; contul creat de test are cleanup explicit. | 62 |

Aceste extensii sunt backlog de business. Testele generice de adnotări,
Playwright, lifecycle, HTML/CSS/JS și layout aparțin modulelor ATF owner.
