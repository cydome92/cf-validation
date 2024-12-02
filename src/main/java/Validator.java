import dto.CittaDto;
import enums.MonthCode;
import enums.Sesso;
import exceptions.MissingCodiceBelfioreException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Validator {

    private static final Pattern VOWELS_PATTERN = Pattern.compile("[aeiou]", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String codiceFiscale, String nome, String cognome, LocalDate dataNascita, CittaDto citta, Sesso sesso, Set<String> belfioreEstero) {
        boolean verifiedCognome = verifySequence(cognome, codiceFiscale.substring(0, 3), 0, 1, 2);
        boolean verifiedNome = verifySequence(nome, codiceFiscale.substring(3, 6), 0, 2, 3);
        boolean verifiedDataNascita = verifyDataNascita(dataNascita, codiceFiscale.substring(6, 8), codiceFiscale.substring(8, 9), codiceFiscale.substring(9, 11), sesso);
        boolean verifiedCodiceBelfiore = isVerifiedCodiceBelfiore(codiceFiscale, citta, belfioreEstero);
        return verifiedCognome && verifiedNome && verifiedDataNascita && verifiedCodiceBelfiore;
    }

    private static boolean verifySequence(String toVerify, String insertedCode, int... indexes) {
        List<Character> chars = new ArrayList<>();
        if (toVerify.replaceAll("\\W", "").length() < 3) {
            chars = collectCharacters(toVerify, Collectors.toList());
            for (int i = chars.size() - 1; i < 3; i++)
                chars.add('X');
        } else {
            Map<Boolean, List<Character>> mapVowels = getMapVowelsConsonants(toVerify);
            if (mapVowels.get(false).size() < indexes[indexes.length - 1] + 1) {
                chars.addAll(mapVowels.get(false));
            } else {
                for (int i : indexes)
                    if (i < mapVowels.get(false).size())
                        chars.add(mapVowels.get(false).get(i));
            }
            if (chars.size() < 3)
                for (int i = 0; chars.size() < 3; i++)
                    chars.add(mapVowels.get(true).get(i));
        }
        String calculatedCode = reduceList(chars);
        return calculatedCode.equalsIgnoreCase(insertedCode);
    }

    private static boolean verifyDataNascita(LocalDate date, String codeYear, String codeMonth, String codeDay, Sesso sesso) {
        String year = date.format(DateTimeFormatter.ofPattern("yy"));
        boolean sameYear = year.equalsIgnoreCase(codeYear);
        boolean sameMonth = MonthCode.valueOf(codeMonth).getMonth().equals(date.getMonth());
        int day = (sesso.equals(Sesso.F) ? 40 : 0) + date.getDayOfMonth();
        boolean sameDay = day == Integer.parseInt(codeDay);
        return sameYear && sameMonth && sameDay;
    }

    private static Map<Boolean, List<Character>> getMapVowelsConsonants(String s) {
        return collectCharacters(s, Collectors.partitioningBy(c -> VOWELS_PATTERN.matcher(Character.toString(c)).find()));
    }

    private static <A, Z> Z collectCharacters(String s, Collector<Character, A, Z> collector) {
        return s.replaceAll("\\W", "").chars()
                .mapToObj(c -> (char) c)
                .collect(collector);
    }

    private static boolean isVerifiedCodiceBelfiore(String codiceFiscale, CittaDto citta, Set<String> belfioreEstero) {
        boolean isEstero = citta.nome().equalsIgnoreCase("estero");
        boolean verifiedCodiceBelfiore = false;
        String belfioreInserito = codiceFiscale.substring(11, 15);
        if (!isEstero) {
            if (citta.codiceBelfiore() == null || citta.codiceBelfiore().isEmpty())
                throw new MissingCodiceBelfioreException();
            verifiedCodiceBelfiore = citta.codiceBelfiore().equalsIgnoreCase(belfioreInserito);
        } else {
            verifiedCodiceBelfiore = belfioreEstero.contains(belfioreInserito);
        }
        return verifiedCodiceBelfiore;
    }

    private static String reduceList(List<Character> listChars) {
        return listChars.stream()
                .map(String::valueOf)
                .reduce("", (p, s) -> p.isEmpty() ? s : p.concat(s));
    }

}
