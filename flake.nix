{
  description = "tagless — type-safe HTML DSL for Scala 3 (Zipper-based)";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = nixpkgs.legacyPackages.${system};
    in {
      devShells.${system}.default = pkgs.mkShell {
        name = "tagless-dev";
        nativeBuildInputs = with pkgs; [
          mill
          scala-cli
          openjdk
          nodejs        # for example dev / Vite consumers
        ];
      };
    };
}
